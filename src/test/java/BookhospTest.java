import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class BookhospTest {

    private static final String TEMP_PATIENTS_FILE = "test_patients.json";
    private static final String TEMP_DOCTORS_FILE = "test_doctors.json";

    @BeforeEach
    void setupTestFiles() throws IOException {
        JSONArray patients = new JSONArray();
        patients.put(new JSONObject()
                .put("patient_id", "P001")
                        .put("name","Test 1")
                        .put("age",35)
                        .put("gender", "male")
                        .put("date_of_birth", "15/01/1998")
                .put("appointments", new JSONArray()));
        patients.put(new JSONObject()
                .put("patient_id", "P002")
                .put("name","Test 2")
                .put("age",57)
                .put("gender", "female")
                .put("date_of_birth", "15/05/1968")
                .put("appointments", new JSONArray()));

        JSONArray doctors = new JSONArray();
        doctors.put(new JSONObject()
                .put("doctor_id", "D001")
                .put("department", "GENERAL")
                .put("schedule", new JSONArray()));
        doctors.put(new JSONObject()
                .put("doctor_id", "D002")
                .put("department", "PEDIATRICS")
                .put("schedule", new JSONArray()));


        Files.write(Paths.get(TEMP_PATIENTS_FILE), patients.toString().getBytes());
        Files.write(Paths.get(TEMP_DOCTORS_FILE), doctors.toString().getBytes());
    }

    @AfterEach
    void cleanupTestFiles() {
        new File(TEMP_PATIENTS_FILE).delete();
        new File(TEMP_DOCTORS_FILE).delete();
    }
}
