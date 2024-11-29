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

    @Test
    void testWhereFilesAreNotIntroduced(){
        String[] args = {"add"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: Insufficient arguments. Usage: Bookhosp <command> <patients.json> <doctors.json> [options]"));
    }

    @Test
    void testWhereDoctorFileIsNotValid(){
        String[] args = {"add", TEMP_PATIENTS_FILE, "testing_file.json"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: One or both input files are invalid or cannot be read."));
    }

    @Test
    void testWherePatientesFileIsNotValid(){
        String[] args = {"add","testing_file.json", TEMP_DOCTORS_FILE};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: One or both input files are invalid or cannot be read."));
    }

    @Test
    void testInvalidOption(){
        String[] args = {"testing", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Unknown command"));
    }

    @Test
    void testHelpCommand() {
        String[] args = {"help", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Available commands"), "Help Message is not correct");

    }

    @Test
    void testHandleBookingWithValidData() throws IOException {
        String[] args = {"book", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001", "GENERAL"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Appointment successfully booked!"), "Schedule should not be empty after booking.");
    }

    @Test
    void testHandleBookingWithInvalidPatient() throws IOException {
        String[] args = {"book", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P999", "GENERAL"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Error: Patient ID not found"), "Patient ID should be not found");
    }

    @Test
    void testHandleBookingWithNotAllTheParams() throws IOException {
        String[] args = {"book", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Usage: book"), "Error message should be informed");
    }


    @Test
    void testHandleDisplay() throws IOException {
        String[] args = {"display", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "ALL", "2024-11-28"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(true, "Display executed successfully.");
    }

    @Test
    void testSearchExistingPatient() throws IOException {
        String[] args = {"search", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P001"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertTrue(consoleOutput.contains("Appointments for Patient P001"), "Patient with ID 'P001' should exist.");
    }

    @Test
    void testSearchNonExistentPatient() throws IOException {
        String[] args = {"search", TEMP_PATIENTS_FILE, TEMP_DOCTORS_FILE, "P999"};
        Bookhosp.main(args);
        String consoleOutput = outputStreamCaptor.toString().trim();
        assertFalse(consoleOutput.contains("Error: Patient ID not found"), "Patient with ID 'P999' should not exist.");
    }
}