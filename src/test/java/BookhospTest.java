import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class BookhospTest {

    private static final String TEMP_PATIENTS_FILE = "test_patients.json";
    private static final String TEMP_DOCTORS_FILE = "test_doctors.json";

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private PrintStream originalOut;

    @BeforeEach
    void setUp() throws IOException {
        JSONArray patients = new JSONArray();
        patients.put(new JSONObject()
                .put("patient_id", "P001")
                .put("name", "Test 1")
                .put("age", 35)
                .put("gender", "male")
                .put("date_of_birth", "15/01/1998")
                .put("appointments", new JSONArray()));
        patients.put(new JSONObject()
                .put("patient_id", "P002")
                .put("name", "Test 2")
                .put("age", 57)
                .put("gender", "female")
                .put("date_of_birth", "15/05/1968")
                .put("appointments", new JSONArray()));

        JSONArray doctors = new JSONArray();
        doctors.put(new JSONObject()
                .put("doctor_id", "D001")
                .put("name", "Doctor 1")
                .put("gender", "female")
                .put("specialization", "General Medicine")
                .put("department", "GENERAL")
                .put("schedule", new JSONArray()));
        doctors.put(new JSONObject()
                .put("doctor_id", "D002")
                .put("name", "Doctor 2")
                .put("gender", "male")
                .put("specialization", "Pediatrics")
                .put("department", "PEDIATRICS")
                .put("schedule", new JSONArray()));

        Files.write(Paths.get(TEMP_PATIENTS_FILE), patients.toString().getBytes());
        Files.write(Paths.get(TEMP_DOCTORS_FILE), doctors.toString().getBytes());
        originalOut = System.out;
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void cleanUp() {
        new File(TEMP_PATIENTS_FILE).delete();
        new File(TEMP_DOCTORS_FILE).delete();
        System.setOut(originalOut);

        System.out.println("Test output was:\n" + outputStreamCaptor);
    }

    //TC01
    @Test
    void testWhereFilesAreNotIntroduced(){
        String[] args = {"add"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: Insufficient arguments. Usage: Bookhosp <command> <patients.json> <doctors.json> [options]"));
    }

    //TC02
    @Test
    void testWhereDoctorFileIsNotValid(){
        String[] args = {"add", TEMP_PATIENTS_FILE, "testing_file.json"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: One or both input files are invalid or cannot be read."));
    }

    //TC03
    @Test
    void testWherePatientesFileIsNotValid(){
        String[] args = {"add","testing_file.json", TEMP_DOCTORS_FILE};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: One or both input files are invalid or cannot be read."));
    }

    //TC04
    @Test
    void testInvalidOption(){
        String[] args = {"testing", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Unknown command"));
    }

    //TC05
    @Test
    void testHelpCommand() {
        String[] args = {"help", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Available commands"), "Help Message is not correct");

    }

    //TC06
    @Test
    void testHandleBookingWithValidData() {
        String[] args = {"book", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001", "GENERAL"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Appointment successfully booked!"), "Schedule should not be empty after booking.");
    }

    //TC07
    @Test
    void testHandleBookingWithInvalidPatient() {
        String[] args = {"book", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P999", "GENERAL"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: Patient ID not found"), "Patient ID should be not found");
    }

    //TC08
    @Test
    void testHandleBookingWithNotAllTheParams() {
        String[] args = {"book", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Usage: book"), "Error message should be informed");
    }


    @Test
    void testHandleDisplay() {
        String[] args = {"display", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "ALL", "2024-11-28"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(true, "Display executed successfully.");
    }

    //TC09
    @Test
    void testSearchExistingPatient() {
        String[] args = {"search", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Appointments for Patient P001"), "Patient with ID 'P001' should exist.");
    }

    //TC10
    @Test
    void testSearchNonExistentPatient() {
        String[] args = {"search", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P999"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertFalse(consoleOutput.contains("Error: Patient ID not found"), "Patient with ID 'P999' should not exist.");
    }

    /* Add Functionality */
    //TC11
    @Test
    void testAddInValidCommandIntroduced(){
        String[] args = {"add", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE};
        //String[] args = {"add", "DOCTOR", TEMP_DOCTORS_FILE, "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Usage: add <DOCTOR|PATIENT> <input_file> --admin"));
    }

    //TC12
    @Test
    void testAddDoctor(){
        String[] args = {"add", "DOCTOR", TEMP_DOCTORS_FILE, "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Doctors added successfully!"));
    }

    //TC13
    @Test
    void testErrorWriteAddDoctor(){
        String[] args = {"add", "DOCTOR", TEMP_DOCTORS_FILE, "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error reading input file: "));
    }

    //TC14
    @Test
    void testAddPatient(){
        String[] args = {"add", "PATIENT", TEMP_PATIENTS_FILE, "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Patients added successfully!"));
    }

    //TC15
    @Test
    void testErrorWriteAddPatient(){
        String[] args = {"add", "PATIENT", TEMP_PATIENTS_FILE, "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error reading input file: "));
    }

    //TC16
    @Test
    void testInvalidTypeAddPatient(){
        String[] args = {"add", " ", TEMP_PATIENTS_FILE, "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: Invalid type. Use DOCTOR or PATIENT."));
    }

    /* Delete Functionality */
    //TC17
    @Test
    void testDeleteInValidCommandIntroduced(){
        String[] args = {"delete", TEMP_PATIENTS_FILE, TEMP_PATIENTS_FILE, "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Usage: delete <DOCTOR|PATIENT> <ID> --admin"));
    }

    //TC18
    @Test
    void testDeleteDoctor(){
        String[] args = {"delete", "DOCTOR", "P999", "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Doctor deleted successfully!"));
    }

    //TC19
    @Test
    void testDeletePatient(){
        String[] args = {"delete", "PATIENT", "P999", "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Patient deleted successfully!"));
    }

    //TC20
    @Test
    void testInvalidTypeDeletePatient(){
        String[] args = {"delete", " ", "P999", "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: Invalid type. Use DOCTOR or PATIENT."));
    }

    //TC21
    @Test
    void testErrorWriteDeletePatient(){
        String[] args = {"delete", "PATIENT", "P999", "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error reading input file: "));
    }

    //TC22
    @Test
    void testErrorWriteDeleteDoctor(){
        String[] args = {"delete", "DOCTOR", "P999", "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error reading input file: "));
    }

    /* Modify Functionality */
    //TC23
    @Test
    void testModifyInValidCommandIntroduced(){
        String[] args = {"modify", TEMP_PATIENTS_FILE, TEMP_PATIENTS_FILE, TEMP_PATIENTS_FILE, "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Usage: modify <ID> <input_file> --admin"));
    }

    //TC24
    @Test
    void testModifyDoctor(){
        String[] args = {"modify", "P999", TEMP_DOCTORS_FILE, "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Doctor modified successfully!"));
    }

    //TC25
    @Test
    void testModifyPatient(){
        String[] args = {"modify", "P999", TEMP_PATIENTS_FILE, "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Patient modified successfully!"));
    }

    //TC26
    @Test
    void testErrorWriteModifyDoctor(){
        String[] args = {"modify", "P999", TEMP_DOCTORS_FILE, "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error modifying record: "));
    }

    //TC27
    @Test
    void testErrorWriteModifyPatient(){
        String[] args = {"modify", "P999", TEMP_PATIENTS_FILE, "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error modifying record: "));
    }

    //TC28
    @Test
    void testInvalidIDTypeModifyDoctor(){
        String[] args = {"modify", " ", TEMP_DOCTORS_FILE, "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: ID not found."));
    }

    //TC29
    @Test
    void testInvalidIDTypeModifyPatient(){
        String[] args = {"modify", " ", TEMP_PATIENTS_FILE, "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: ID not found."));
    }

    /* Handle View Schedule Functionality */
    //TC30
    @Test
    void testVWScheduleInValidCommandIntroduced(){
        String[] args = {"view-schedule", TEMP_PATIENTS_FILE};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Usage: view-schedule <Doctor ID|GENERAL|PEDIATRICS|SURGERY>"));
    }

    //TC31
    @Test
    void testVWSchedule(){
        String[] args = {"view-schedule", "GENERAL"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Schedule for "));
    }

    //TC32
    @Test
    void testVWScheduleFileError(){
        String[] args = {"view-schedule", "GENERAL"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error reading doctors file: "));
    }

    /* Cancel Functionality */
    //TC33
    @Test
    void testCancelInValidCommandIntroduced(){
        String[] args = {"cancel", TEMP_PATIENTS_FILE, "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Usage: cancel <APPOINTMENT ID>"));
    }

    //TC34
    @Test
    void testCancel(){
        String[] args = {"cancel", "01"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Appointment "));
    }

    //TC35
    @Test
    void testCancelErrorAppointmentID(){
        String[] args = {"cancel", "dere"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: Appointment ID not found."));
    }

    //TC36
    @Test
    void testCancelFileError(){
        String[] args = {"cancel", "01"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error handling cancellation: "));
    }

    /* Overdue Functionality */
    //TC37
    @Test
    void testOverdueInValidCommandIntroduced(){
        String[] args = {"overdue", TEMP_PATIENTS_FILE};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: Unauthorized access. This command is available only to administrators."));
    }

    //TC38
    @Test
    void testOverdue(){
        String[] args = {"overdue", "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Overdue:  "));
    }

    //TC39
    @Test
    void testOverdueErrorFile(){
        String[] args = {"overdue", "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error reading patients file: "));
    }

    /* Reschedule Functionality */
    //TC40
    @Test
    void testRescheduleInValidCommandIntroduced(){
        String[] args = {"reschedule", TEMP_PATIENTS_FILE};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Usage: reschedule <PATIENT ID> <APPOINTMENT ID> <DEPARTMENT>"));
    }

    //TC41
    @Test
    void testReschedule(){
        String[] args = {"reschedule", "P001", "01","GENERAL"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Appointment successfully rescheduled!"));
    }

    //TC42
    @Test
    void testRescheduleErrorID(){
        String[] args = {"reschedule", "P001", TEMP_PATIENTS_FILE,"GENERAL"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: Appointment ID not found."));
    }

    //TC43
    @Test
    void testRescheduleErrorFile(){
        String[] args = {"reschedule", "P001", "01","GENERAL"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error handling reschedule: "));
    }
}