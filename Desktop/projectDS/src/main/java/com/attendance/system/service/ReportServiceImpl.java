package com.attendance.system.service;

import com.attendance.system.dao.AttendanceDAO;
import com.attendance.system.dao.CourseDAO;
import com.attendance.system.dao.UserDAO;
import com.attendance.system.exception.DatabaseException;
import com.attendance.system.model.AttendanceRecord;
import com.attendance.system.model.AttendanceStatus;
import com.attendance.system.model.Course;
import com.attendance.system.model.Student;
import com.attendance.system.model.Teacher;
import com.attendance.system.model.User;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of ReportService providing attendance report generation with filtering,
 * statistics calculation, and export functionality.
 */
public class ReportServiceImpl implements ReportService {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final double ATTENDANCE_THRESHOLD = 75.0;

    private final AttendanceDAO attendanceDAO;
    private final CourseDAO courseDAO;
    private final UserDAO userDAO;

    public ReportServiceImpl() {
        this.attendanceDAO = new AttendanceDAO();
        this.courseDAO = new CourseDAO();
        this.userDAO = new UserDAO();
    }

    public ReportServiceImpl(AttendanceDAO attendanceDAO, CourseDAO courseDAO, UserDAO userDAO) {
        this.attendanceDAO = attendanceDAO;
        this.courseDAO = courseDAO;
        this.userDAO = userDAO;
    }

    @Override
    public Map<String, Object> generateAttendanceReport(ReportCriteria criteria) throws DatabaseException {
        Map<String, Object> reportData = new HashMap<>();

        // Retrieve filtered attendance records
        List<AttendanceRecord> records = attendanceDAO.findWithFilters(
            criteria.getStudentId() != null ? criteria.getStudentId() : -1,
            criteria.getCourseId() != null ? criteria.getCourseId() : -1,
            criteria.getStartDate(),
            criteria.getEndDate()
        );

        // Filter by attendance status if specified
        if (criteria.getAttendanceStatus() != null && !criteria.getAttendanceStatus().isEmpty()) {
            records = records.stream()
                .filter(r -> r.getStatus().toString().equals(criteria.getAttendanceStatus()))
                .collect(Collectors.toList());
        }

        // Calculate statistics
        Map<String, Object> statistics = calculateReportStatistics(records);

        // Create summary
        Map<String, Object> summary = createReportSummary(records, statistics);

        reportData.put("records", records);
        reportData.put("statistics", statistics);
        reportData.put("summary", summary);
        reportData.put("criteria", criteria);
        reportData.put("generatedDate", LocalDate.now());

        return reportData;
    }

    @Override
    public Map<String, Object> generateSystemStatistics(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        Map<String, Object> statistics = new HashMap<>();

        // Get all attendance records for the period
        List<AttendanceRecord> allRecords = attendanceDAO.findByDateRange(startDate, endDate);

        // Total records
        statistics.put("totalRecords", allRecords.size());

        // Calculate average attendance percentage
        double averageAttendance = calculateAverageAttendance(allRecords);
        statistics.put("averageAttendance", Math.round(averageAttendance * 100.0) / 100.0);

        // Attendance by status
        Map<String, Long> attendanceByStatus = allRecords.stream()
            .collect(Collectors.groupingBy(
                r -> r.getStatus().getDisplayName(),
                Collectors.counting()
            ));
        statistics.put("attendanceByStatus", attendanceByStatus);

        // Identify at-risk students (below 75% attendance)
        List<Integer> atRiskStudents = attendanceDAO.getStudentsWithLowAttendance(-1, ATTENDANCE_THRESHOLD);
        statistics.put("atRiskStudents", atRiskStudents.size());

        // Attendance trends by date
        Map<LocalDate, Long> trends = allRecords.stream()
            .collect(Collectors.groupingBy(
                AttendanceRecord::getAttendanceDate,
                Collectors.counting()
            ));
        statistics.put("trends", trends);

        // Top performing students (highest attendance)
        Map<Integer, Double> studentAttendanceMap = new HashMap<>();
        for (AttendanceRecord record : allRecords) {
            studentAttendanceMap.putIfAbsent(record.getStudentId(), 0.0);
            if (record.getStatus().countsAsAttended()) {
                studentAttendanceMap.put(record.getStudentId(), 
                    studentAttendanceMap.get(record.getStudentId()) + 1);
            }
        }

        List<Map.Entry<Integer, Double>> topStudents = studentAttendanceMap.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(10)
            .collect(Collectors.toList());
        statistics.put("topStudents", topStudents);

        statistics.put("generatedDate", LocalDate.now());
        statistics.put("period", "From " + startDate + " to " + endDate);

        return statistics;
    }

    @Override
    public Map<String, Object> generateClassReport(int courseId, LocalDate startDate, LocalDate endDate) throws DatabaseException {
        Map<String, Object> reportData = new HashMap<>();

        // Get course information
        Course course = courseDAO.findById(courseId);
        reportData.put("courseInfo", course);

        // Get enrolled students
        List<Student> enrolledStudents = courseDAO.getEnrolledStudents(courseId);

        // Get attendance records for the course
        List<AttendanceRecord> records = new ArrayList<>();
        for (Student student : enrolledStudents) {
            List<AttendanceRecord> studentRecords = attendanceDAO.findWithFilters(
                student.getUserId(),
                courseId,
                startDate,
                endDate
            );
            records.addAll(studentRecords);
        }

        // Calculate statistics
        Map<String, Object> statistics = calculateReportStatistics(records);
        statistics.put("enrolledStudentCount", enrolledStudents.size());

        // Create summary
        Map<String, Object> summary = createReportSummary(records, statistics);

        reportData.put("records", records);
        reportData.put("statistics", statistics);
        reportData.put("summary", summary);
        reportData.put("enrolledStudents", enrolledStudents);
        reportData.put("generatedDate", LocalDate.now());

        return reportData;
    }

    @Override
    public Map<String, Object> generateStudentReport(int studentId, LocalDate startDate, LocalDate endDate) throws DatabaseException {
        Map<String, Object> reportData = new HashMap<>();

        // Get student information
        User user = userDAO.findById(studentId);
        reportData.put("studentInfo", user);

        // Get attendance records for the student
        List<AttendanceRecord> records = attendanceDAO.findWithFilters(
            studentId,
            -1,
            startDate,
            endDate
        );

        // Calculate statistics
        Map<String, Object> statistics = calculateReportStatistics(records);

        // Create summary
        Map<String, Object> summary = createReportSummary(records, statistics);

        reportData.put("records", records);
        reportData.put("statistics", statistics);
        reportData.put("summary", summary);
        reportData.put("generatedDate", LocalDate.now());

        return reportData;
    }

    @Override
    public byte[] exportToPDF(Map<String, Object> reportData, String fileName) throws DatabaseException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Add title
            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            Paragraph title = new Paragraph("Attendance Report")
                .setFont(titleFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Add generation date
            PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            Paragraph dateInfo = new Paragraph("Generated: " + LocalDate.now())
                .setFont(normalFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
            document.add(dateInfo);

            // Add summary section
            @SuppressWarnings("unchecked")
            Map<String, Object> summary = (Map<String, Object>) reportData.get("summary");
            if (summary != null) {
                Paragraph summaryTitle = new Paragraph("Summary")
                    .setFont(titleFont)
                    .setFontSize(12);
                document.add(summaryTitle);

                for (Map.Entry<String, Object> entry : summary.entrySet()) {
                    Paragraph summaryItem = new Paragraph(entry.getKey() + ": " + entry.getValue())
                        .setFont(normalFont)
                        .setFontSize(10);
                    document.add(summaryItem);
                }
            }

            // Add records table
            @SuppressWarnings("unchecked")
            List<AttendanceRecord> records = (List<AttendanceRecord>) reportData.get("records");
            if (records != null && !records.isEmpty()) {
                Paragraph recordsTitle = new Paragraph("Attendance Records")
                    .setFont(titleFont)
                    .setFontSize(12);
                document.add(recordsTitle);

                Table table = new Table(5);
                table.addCell(new Cell().add(new Paragraph("Student ID").setFont(titleFont)));
                table.addCell(new Cell().add(new Paragraph("Course ID").setFont(titleFont)));
                table.addCell(new Cell().add(new Paragraph("Date").setFont(titleFont)));
                table.addCell(new Cell().add(new Paragraph("Status").setFont(titleFont)));
                table.addCell(new Cell().add(new Paragraph("Remarks").setFont(titleFont)));

                for (AttendanceRecord record : records) {
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(record.getStudentId()))));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(record.getCourseId()))));
                    table.addCell(new Cell().add(new Paragraph(record.getAttendanceDate().format(DATE_FORMATTER))));
                    table.addCell(new Cell().add(new Paragraph(record.getStatus().getDisplayName())));
                    table.addCell(new Cell().add(new Paragraph(record.getRemarks() != null ? record.getRemarks() : "")));
                }

                document.add(table);
            }

            document.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new DatabaseException("Failed to export report to PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] exportToExcel(Map<String, Object> reportData, String fileName) throws DatabaseException {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Attendance Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Student ID");
            headerRow.createCell(1).setCellValue("Course ID");
            headerRow.createCell(2).setCellValue("Date");
            headerRow.createCell(3).setCellValue("Status");
            headerRow.createCell(4).setCellValue("Remarks");

            // Add records
            @SuppressWarnings("unchecked")
            List<AttendanceRecord> records = (List<AttendanceRecord>) reportData.get("records");
            int rowNum = 1;
            if (records != null) {
                for (AttendanceRecord record : records) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(record.getStudentId());
                    row.createCell(1).setCellValue(record.getCourseId());
                    row.createCell(2).setCellValue(record.getAttendanceDate().format(DATE_FORMATTER));
                    row.createCell(3).setCellValue(record.getStatus().getDisplayName());
                    row.createCell(4).setCellValue(record.getRemarks() != null ? record.getRemarks() : "");
                }
            }

            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            @SuppressWarnings("unchecked")
            Map<String, Object> summary = (Map<String, Object>) reportData.get("summary");
            int summaryRow = 0;
            if (summary != null) {
                for (Map.Entry<String, Object> entry : summary.entrySet()) {
                    Row row = summarySheet.createRow(summaryRow++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(String.valueOf(entry.getValue()));
                }
            }

            summarySheet.autoSizeColumn(0);
            summarySheet.autoSizeColumn(1);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new DatabaseException("Failed to export report to Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Calculates statistics for a list of attendance records.
     */
    private Map<String, Object> calculateReportStatistics(List<AttendanceRecord> records) {
        Map<String, Object> statistics = new HashMap<>();

        if (records.isEmpty()) {
            statistics.put("totalRecords", 0);
            statistics.put("presentCount", 0);
            statistics.put("absentCount", 0);
            statistics.put("lateCount", 0);
            statistics.put("excusedCount", 0);
            statistics.put("attendancePercentage", 0.0);
            return statistics;
        }

        long presentCount = records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
            .count();
        long absentCount = records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.ABSENT)
            .count();
        long lateCount = records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.LATE)
            .count();
        long excusedCount = records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.EXCUSED)
            .count();

        double attendancePercentage = ((presentCount + lateCount) / (double) records.size()) * 100;

        statistics.put("totalRecords", records.size());
        statistics.put("presentCount", presentCount);
        statistics.put("absentCount", absentCount);
        statistics.put("lateCount", lateCount);
        statistics.put("excusedCount", excusedCount);
        statistics.put("attendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0);

        return statistics;
    }

    /**
     * Creates a summary of the report.
     */
    private Map<String, Object> createReportSummary(List<AttendanceRecord> records, Map<String, Object> statistics) {
        Map<String, Object> summary = new HashMap<>();

        summary.put("Total Records", statistics.get("totalRecords"));
        summary.put("Present", statistics.get("presentCount"));
        summary.put("Absent", statistics.get("absentCount"));
        summary.put("Late", statistics.get("lateCount"));
        summary.put("Excused", statistics.get("excusedCount"));
        summary.put("Attendance Percentage", statistics.get("attendancePercentage") + "%");

        return summary;
    }

    /**
     * Calculates average attendance percentage from records.
     */
    private double calculateAverageAttendance(List<AttendanceRecord> records) {
        if (records.isEmpty()) {
            return 0.0;
        }

        long attendedCount = records.stream()
            .filter(r -> r.getStatus().countsAsAttended())
            .count();

        return (attendedCount / (double) records.size()) * 100;
    }
}
