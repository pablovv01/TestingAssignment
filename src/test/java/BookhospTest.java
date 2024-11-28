import org.json.JSONArray;
import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class BookhospTest {

    private static File patientsFile;
    private static File doctorsFile;

    @BeforeAll
    static void setup() throws IOException {
        patientsFile = File.createTempFile("patients", ".json");
        doctorsFile = File.createTempFile("doctors", ".json");

        String patientsData = """
                [
                    {
                        "patient_id": "P001",
                        "name": "John Doe",
                        "appointments": [
                            {
                                "appointment_id": "A001",
                                "date": "2024-11-25",
                                "status": "Scheduled"
                            }
                        ]
                    }
                ]
                """;

        String doctorsData = """
                [
                    {
                        "doctor_id": "D001",
                        "name": "Dr. Smith",
                        "department": "GENERAL",
                        "schedule": [
                            {
                                "date": "2024-11-30",
                                "appointments": []
                            }
                        ]
                    }
                ]
                """;

        Files.write(Paths.get(patientsFile.getAbsolutePath()), patientsData.getBytes());
        Files.write(Paths.get(doctorsFile.getAbsolutePath()), doctorsData.getBytes());
    }

    @AfterAll
    static void tearDown() {
        patientsFile.delete();
        doctorsFile.delete();
    }

    @Test
    void exampleTest() throws IOException {

        String[] args = {
                "reschedule",
                "P001",
                "A001",
                "GENERAL"
        };

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream)); // Capturar salida
        Bookhosp.main(new String[]{
                "reschedule",
                "P001",
                "A001",
                "GENERAL",
                patientsFile.getAbsolutePath(),
                doctorsFile.getAbsolutePath()
        });

        JSONArray updatedPatients = new JSONArray(Files.readString(Paths.get(patientsFile.getAbsolutePath())));
        JSONArray updatedDoctors = new JSONArray(Files.readString(Paths.get(doctorsFile.getAbsolutePath())));

        assertTrue(updatedPatients.getJSONObject(0).getJSONArray("appointments").isEmpty(),
                "La cita debería haberse eliminado del paciente.");

        JSONArray doctorSchedule = updatedDoctors.getJSONObject(0).getJSONArray("schedule");
        JSONArray doctorAppointments = doctorSchedule.getJSONObject(0).getJSONArray("appointments");
        assertEquals(1, doctorAppointments.length(), "La cita debería haberse agregado al doctor.");
        assertEquals("P001", doctorAppointments.getJSONObject(0).getString("patient_id"),
                "La cita debe pertenecer al paciente correcto.");


        String output = outputStream.toString();
        assertTrue(output.contains("Appointment successfully rescheduled!"),
                "El mensaje esperado no está presente en la salida.");
    }
}
