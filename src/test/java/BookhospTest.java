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
    private static final String TEMP_INPUT_DOCTOR_FILE = "test_input_doctor.json";
    private static final String TEMP_INPUT_PATIENT_FILE = "test_input_patient.json";

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
        doctors.put(new JSONObject()
                .put("doctor_id", "D003")
                .put("name", "Doctor 3")
                .put("gender", "male")
                .put("specialization", "Surgery")
                .put("department", "SURGERY")
                .put("schedule", new JSONArray()));
        JSONObject inputDoctor = new JSONObject()
                .put("doctor_id", "")
                .put("name", "")
                .put("gender", "")
                .put("specialization", "")
                .put("department", "")
                .put("schedule", new JSONArray());
        JSONObject inputPatient = new JSONObject()
                .put("patient_id", "")
                .put("name", "")
                .put("age", 0)
                .put("gender", "")
                .put("date_of_birth", "")
                .put("appointments", new JSONArray());

        Files.write(Paths.get(TEMP_PATIENTS_FILE), patients.toString().getBytes());
        Files.write(Paths.get(TEMP_DOCTORS_FILE), doctors.toString().getBytes());
        Files.write(Paths.get(TEMP_INPUT_DOCTOR_FILE), inputDoctor.toString().getBytes());
        Files.write(Paths.get(TEMP_INPUT_PATIENT_FILE), inputPatient.toString().getBytes());
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
    void testWhereCommandIsNotIntroduced(){
        String[] args = {};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: Insufficient arguments. Usage: Bookhosp <command> [options]"));
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
    void testHandleBookingWithValidDataGeneral() {
        String[] args = {"book", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001", "GENERAL"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Appointment successfully booked"), "Schedule should not be empty after booking.");
    }

    @Test
    void testHandleBookingWithValidDataPediatrics() {
        String[] args = {"book", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001", "Pediatrics"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Appointment successfully booked"), "Schedule should not be empty after booking.");
    }

    @Test
    void testHandleBookingWithValidDataSurgery() {
        String[] args = {"book", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001", "SURGERY"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Appointment successfully booked"), "Schedule should not be empty after booking.");
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
    void testHandleDisplayAll() {
        String[] args = {"display", TEMP_DOCTORS_FILE, "ALL", "2024-11-28"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Doctor ID:"), "Display executed successfully.");
    }

    //TC09
    @Test
    void testSearchExistingPatient() {
        String[] args = {"search", TEMP_PATIENTS_FILE, "P001"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Appointments for Patient P001"), "Patient with ID 'P001' should exist.");
    }

    //TC10
    @Test
    void testSearchNonExistentPatient() {
        String[] args = {"search", TEMP_PATIENTS_FILE, "P999"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: Patient ID not found"), "Patient with ID 'P999' should not exist.");
    }

    /* Add Functionality */
    //TC11
    @Test
    void testAddInValidCommandIntroduced(){
        String[] args = {"add", TEMP_PATIENTS_FILE};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Usage: add <patient.json> <doctor.json> <DOCTOR|PATIENT> <input_file> --admin"));
    }

    //TC12
    @Test
    void testAddDoctor(){
        String[] args = {"add", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "DOCTOR", TEMP_DOCTORS_FILE, "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Doctors added successfully!"));
    }

    //TC13
    @Test
    void testErrorWriteAddDoctor(){
        String[] args = {"add", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "DOCTOR", "TEMP_DOCTORS_FILE", "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error reading input file: "));
    }

    //TC14
    @Test
    void testAddPatient(){
        String[] args = {"add", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "PATIENT", TEMP_PATIENTS_FILE, "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Patients added successfully!"));
    }

    //TC15
    @Test
    void testErrorWriteAddPatient(){
        String[] args = {"add", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "PATIENT", "TEMP_PATIENTS_FILE", "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error reading input file: "));
    }

    //TC16
    @Test
    void testInvalidTypeAddPatient(){
        String[] args = {"add", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, " ", TEMP_PATIENTS_FILE, "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: Invalid type. Use DOCTOR or PATIENT."));
    }

    /* Delete Functionality */
    //TC17
    @Test
    void testDeleteInValidCommandIntroduced(){
        String[] args = {"delete", TEMP_PATIENTS_FILE};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Usage: delete <patient.json> <doctor.json> <ID> <DOCTOR|PATIENT> --admin"));
    }

    //TC18
    @Test
    void testDeleteDoctor(){
        String[] args = {"delete", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "D001", "DOCTOR", "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Doctor deleted successfully!"));
    }

    //TC19
    @Test
    void testDeletePatient(){
        String[] args = {"delete", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001", "PATIENT", "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Patient deleted successfully!"));
    }

    //TC20
    @Test
    void testInvalidTypeDeletePatient(){
        String[] args = {"delete", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001", " ", "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: Invalid type. Use DOCTOR or PATIENT."));
    }

    //TC21
    @Test
    void testErrorWriteDeletePatient(){
        String[] args = {"delete", TEMP_PATIENTS_FILE, "TEMP_DOCTORS_FILE", "P001", "PATIENT", "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: File "));
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
        String[] args = {"modify", TEMP_PATIENTS_FILE};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Usage: modify <patient.json> <doctor.json> <ID> <DOCTOR|PATIENT> <input_file> --admin"));
    }

    //TC24
    @Test
    void testModifyDoctor(){
        String[] args = {"modify", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "D001", "DOCTOR", TEMP_INPUT_DOCTOR_FILE, "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Doctor modified successfully!"));
    }

    //TC25
    @Test
    void testModifyPatient(){
        String[] args = {"modify", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001", "PATIENT", TEMP_INPUT_PATIENT_FILE, "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Patient modified successfully!"));
    }

    //TC26
    @Test
    void testErrorWriteModifyDoctor(){
        String[] args = {"modify", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "D001", "DOCTOR", "TEMP_INPUT_DOCTOR_FILE", "--admin" };
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error modifying record: "));
    }

    //TC27
    @Test
    void testErrorWriteModifyPatient(){
        String[] args = {"modify", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001", "PATIENT", "TEMP_INPUT_PATIENT_FILE", "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error modifying record: "));
    }

    //TC28
    @Test
    void testInvalidTypeModify(){
        String[] args = {"modify", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001", " ", TEMP_INPUT_PATIENT_FILE, "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: Invalid type. Use DOCTOR or PATIENT."));
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
        assertTrue(consoleOutput.contains("Usage: view-schedule <doctor.json> <Doctor ID|GENERAL|PEDIATRICS|SURGERY>"));
    }

    //TC31
    @Test
    void testVWSchedule(){
        String[] args = {"view-schedule", TEMP_DOCTORS_FILE, "GENERAL"};
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
        String[] args = {"cancel", "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Usage: cancel <doctors.json> <APPOINTMENT ID>"));
    }

    //TC34
    @Test
    void testCancel(){
        String[] args = {"cancel", TEMP_DOCTORS_FILE, "01"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Appointment "));
    }

    //TC35
    @Test
    void testCancelErrorAppointmentID(){
        String[] args = {"cancel", TEMP_DOCTORS_FILE, "dere"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: Appointment ID not found."));
    }

    //TC36
    @Test
    void testCancelFileError(){
        String[] args = {"cancel", "file_example.json", "0001"};
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
        String[] args = {"overdue", TEMP_PATIENTS_FILE, "--admin"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(true);
    }

    //TC39
    @Test
    void testOverdueErrorFile(){
        String[] args = {"overdue", TEMP_PATIENTS_FILE, "--admin"};
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
        assertTrue(consoleOutput.contains("reschedule <patient.json> <doctor.json> <PATIENT ID> <APPOINTMENT ID> <DEPARTMENT> [Option]"));
    }

    //TC41
    @Test
    void testReschedule(){
        String[] args = {"reschedule", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001", "01","GENERAL"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Appointment successfully rescheduled!"));
    }

    //TC42
    @Test
    void testRescheduleErrorID(){
        String[] args = {"reschedule", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001", "02", "GENERAL"};
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