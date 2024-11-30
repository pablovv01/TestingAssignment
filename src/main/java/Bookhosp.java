import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Bookhosp {

    private static final String LOG_FILE = "log.txt";

    public static void main(String[] args) {
        if (args.length < 1) {
            log("Error: Insufficient arguments. Usage: Bookhosp <command> [options]");
            return;
        }

        String command = args[0].toLowerCase();

        switch (command) {
            case "help":
                displayHelp();
                break;
            case "display":
                handleDisplay(args);
                break;
            case "book":
                handleBooking(args);
                break;
            case "search":
                handleSearch(args);
                break;
            case "cancel":
                handleCancel(args);
                break;
            case "overdue":
                handleOverdue(args);
                break;
            case "reschedule":
                handleReschedule(args);
                break;
            case "add":
                handleAdd(args);
                break;
            case "modify":
                handleModify(args);
                break;
            case "delete":
                handleDelete(args);
                break;
            case "view-schedule":
                handleViewSchedule(args);
                break;
            default:
                log("Error: Unknown command. Use 'help' to list all commands.");
        }
    }

    private static void displayHelp() {
        System.out.println("""
                    Available commands:
                    1. help
                    2. display <ALL|GENERAL|PEDIATRICS|SURGERY|DOCTOR <Doctor_ID>> <DATE>
                    3. book <PATIENT ID> <DEPARTMENT>
                    4. search <PATIENT ID>
                    5. cancel <APPOINTMENT ID>
                    6. overdue --admin
                    7. reschedule <PATIENT ID> <APPOINTMENT ID> <DEPARTMENT>
                    8. add <DOCTOR|PATIENT> <input_file> --admin
                    9. modify <ID> <input_file> --admin
                    10. delete <ID> --admin
                    11. view-schedule <Doctor ID|GENERAL|PEDIATRICS|SURGERY>
                """);
    }

    private static void log(String message) {
        System.out.println(message);
        if (Objects.equals("--log", System.getProperty("log"))) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                writer.write(LocalDate.now() + " " + message + "\n");
            } catch (IOException e) {
                System.err.println("Failed to write to log file.");
            }
        }
    }

    private static void handleReschedule(String[] args) {
        if (args.length < 6) {
            log("Usage: reschedule <patient.json> <doctor.json> <PATIENT ID> <APPOINTMENT ID> <DEPARTMENT> [Option]");
            return;
        }

        String patientsFile = args[1];
        String doctorsFile = args[2];
        String patientId = args[3];
        String appointmentId = args[4];
        String department = args[5].toUpperCase();

        try {
            Path doctorPath = Paths.get(doctorsFile);
            Path patientPath = Paths.get(patientsFile);

            JSONArray patients = new JSONArray(new String(Files.readAllBytes(patientPath)));
            JSONArray doctors = new JSONArray(new String(Files.readAllBytes(doctorPath)));

            boolean appointmentFound = false;
            for (int i = 0; i < patients.length(); i++) {
                JSONObject patient = patients.getJSONObject(i);
                if (patient.getString("patient_id").equals(patientId)) {
                    JSONArray appointments = patient.getJSONArray("appointments");
                    for (int j = 0; j < appointments.length(); j++) {
                        JSONObject appointment = appointments.getJSONObject(j);
                        if (appointment.getString("appointment_id").equals(appointmentId)) {
                            appointmentFound = true;
                            for (int k = 0; k < doctors.length(); k++) {
                                JSONObject doctor = doctors.getJSONObject(k);
                                if (doctor.getString("department").equalsIgnoreCase(department)) {
                                    JSONArray schedule = doctor.getJSONArray("schedule");
                                    for (int l = 0; l < schedule.length(); l++) {
                                        JSONObject day = schedule.getJSONObject(l);
                                        if (day.getJSONArray("appointments").length() < 10) {
                                            log(String.valueOf(day.getJSONArray("appointments").length()));
                                            day.getJSONArray("appointments").put(appointment);
                                            appointments.remove(j);
                                            Files.write(patientPath, patients.toString().getBytes());
                                            Files.write(doctorPath, doctors.toString().getBytes());
                                            log("Appointment successfully rescheduled!");
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!appointmentFound) {
                log("Error: Appointment ID not found.");
            }
        } catch (IOException e) {
            log("Error handling reschedule: " + e.getMessage());
        }
    }

    private static void handleAdd(String[] args) {
        if (args.length < 6 || !args[5].equalsIgnoreCase("--admin")) {
            log("Usage: add <patient.json> <doctor.json> <DOCTOR|PATIENT> <input_file> --admin");
            return;
        }

        String patientsFile = args[1];
        String doctorsFile = args[2];
        String type = args[3].toUpperCase();
        String inputFile = args[4];


        try {
            String inputData = new String(Files.readAllBytes(Paths.get(inputFile)));
            JSONArray data = new JSONArray(inputData);

            if (type.equals("DOCTOR")) {
                JSONArray doctors = new JSONArray(new String(Files.readAllBytes(Paths.get(doctorsFile))));
                for (int i = 0; i < data.length(); i++) {
                    doctors.put(data.getJSONObject(i));
                }
                Files.write(Paths.get(doctorsFile), doctors.toString().getBytes());
                log("Doctors added successfully!");
            } else if (type.equals("PATIENT")) {
                JSONArray patients = new JSONArray(new String(Files.readAllBytes(Paths.get(patientsFile))));
                for (int i = 0; i < data.length(); i++) {
                    patients.put(data.getJSONObject(i));
                }
                Files.write(Paths.get(patientsFile), patients.toString().getBytes());
                log("Patients added successfully!");
            } else {
                log("Error: Invalid type. Use DOCTOR or PATIENT.");
            }
        } catch (IOException e) {
            log("Error reading input file: " + e.getMessage());
        }
    }

    private static void handleModify(String[] args) {
        if (args.length < 7 || !args[6].equalsIgnoreCase("--admin")) {
            log("Usage: modify <patient.json> <doctor.json> <ID> <DOCTOR|PATIENT> <input_file> --admin");
            return;
        }
        String patientsFile = args[1];
        String doctorsFile = args[2];
        String id = args[3];
        String type = args[4].toUpperCase();
        String inputFile = args[5];

        try {
            String inputData = new String(Files.readAllBytes(Paths.get(inputFile)));
            JSONObject newData = new JSONObject(inputData);
            if (type.equals("DOCTOR")) {
                JSONArray doctors = new JSONArray(new String(Files.readAllBytes(Paths.get(doctorsFile))));
                for (int i = 0; i < doctors.length(); i++) {
                    if (doctors.getJSONObject(i).getString("doctor_id").equals(id)) {
                        doctors.put(i, newData);
                        Files.write(Paths.get(doctorsFile), doctors.toString().getBytes());
                        log("Doctor modified successfully!");
                        return;
                    }
                }
            } else if (type.equals("PATIENT")) {
                JSONArray patients = new JSONArray(new String(Files.readAllBytes(Paths.get(patientsFile))));
                for (int i = 0; i < patients.length(); i++) {
                    if (patients.getJSONObject(i).getString("patient_id").equals(id)) {
                        patients.put(i, newData);
                        Files.write(Paths.get(patientsFile), patients.toString().getBytes());
                        log("Patient modified successfully!");
                        return;
                    }
                }
            }else{
                log("Error: Invalid type. Use DOCTOR or PATIENT.");
            }

        } catch (IOException e) {
            log("Error modifying record: " + e.getMessage());
        }
    }

    private static void handleDelete(String[] args) {
        if (args.length < 6 || !args[5].equalsIgnoreCase("--admin")) {
            log("Usage: delete <patient.json> <doctor.json> <ID> <DOCTOR|PATIENT> --admin");
            return;
        }
        String patientsFile = args[1];
        String doctorsFile = args[2];
        String id = args[3];
        String type = args[4].toUpperCase();

        try {
            if (type.equals("DOCTOR")) {
                JSONArray doctors = new JSONArray(new String(Files.readAllBytes(Paths.get(doctorsFile))));
                for (int i = 0; i < doctors.length(); i++) {
                    if (doctors.getJSONObject(i).getString("doctor_id").equals(id)) {
                        doctors.remove(i);
                        Files.write(Paths.get(doctorsFile), doctors.toString().getBytes());
                        log("Doctor deleted successfully!");
                        return;
                    }
                }
            }else if (type.equals("PATIENT")) {
                JSONArray patients = new JSONArray(new String(Files.readAllBytes(Paths.get(patientsFile))));
                for (int i = 0; i < patients.length(); i++) {
                    if (patients.getJSONObject(i).getString("patient_id").equals(id)) {
                        patients.remove(i);
                        Files.write(Paths.get(patientsFile), patients.toString().getBytes());
                        log("Patient deleted successfully!");
                        return;
                    }
                }
            }else {
                log("Error: Invalid type. Use DOCTOR or PATIENT.");
            }
        } catch (IOException e) {
            log("Error deleting record: " + e.getMessage());
        }
    }

    private static void handleViewSchedule(String[] args) {
        if (args.length < 3) {
            log("Usage: view-schedule <doctor.json> <Doctor ID|GENERAL|PEDIATRICS|SURGERY>");
            return;
        }

        String doctorsFile = args[1];
        String argument = args[2];

        try {
            JSONArray doctors = new JSONArray(new String(Files.readAllBytes(Paths.get(doctorsFile))));
            for (int i = 0; i < doctors.length(); i++) {
                JSONObject doctor = doctors.getJSONObject(i);
                if (argument.equalsIgnoreCase(doctor.getString("department")) ||
                        argument.equalsIgnoreCase(doctor.getString("doctor_id"))) {
                    log("Schedule for " + argument + ": " + doctor.getJSONArray("schedule").toString(2));
                }
            }
        } catch (IOException e) {
            log("Error reading doctors file: " + e.getMessage());
        }
    }

    private static void handleDisplay(String[] args) {
        if (args.length < 4) {
            log("Usage: display <doctor.json> <ALL|GENERAL|PEDIATRICS|SURGERY|DOCTOR <Doctor_ID>> <DATE>");
            return;
        }

        String doctorsFile = args[1];
        String argument = args[2];
        String doctorId = "";
        String date = "";

        if (Objects.equals(argument, "DOCTOR")) {
            if (args.length < 5) {
                log("Usage: display <doctor.json> DOCTOR <Doctor_ID> <DATE>");
                return;
            }
            doctorId = args[3];
            date = args[4];
        } else {
            date = args[3];
        }

        try {
            JSONArray doctors = new JSONArray(new String(Files.readAllBytes(Paths.get(doctorsFile))));

            if (argument.equalsIgnoreCase("ALL") ||
                    argument.equalsIgnoreCase("GENERAL") ||
                    argument.equalsIgnoreCase("PEDIATRICS") ||
                    argument.equalsIgnoreCase("SURGERY")) {

                for (int i = 0; i < doctors.length(); i++) {
                    JSONObject doctor = doctors.getJSONObject(i);

                    // Mostrar todos los doctores si el argumento es ALL
                    if (argument.equalsIgnoreCase("ALL") || doctor.getString("department").equalsIgnoreCase(argument)) {
                        log("Doctor ID: " + doctor.getString("doctor_id") +
                                ", Name: " + doctor.getString("name") +
                                ", Department: " + doctor.getString("department"));

                        JSONArray schedule = doctor.getJSONArray("schedule");
                        for (int j = 0; j < schedule.length(); j++) {
                            JSONObject day = schedule.getJSONObject(j);
                            if (day.getString("date").equals(date)) {
                                log("    " + day.getJSONArray("appointments").length() +
                                        " appointments on " + date);
                            }
                        }
                    }
                }
            } else if (argument.equalsIgnoreCase("DOCTOR")) {
                boolean doctorFound = false;
                for (int i = 0; i < doctors.length(); i++) {
                    JSONObject doctor = doctors.getJSONObject(i);
                    if (doctor.getString("doctor_id").equals(doctorId)) {
                        doctorFound = true;
                        JSONArray schedule = doctor.getJSONArray("schedule");
                        for (int j = 0; j < schedule.length(); j++) {
                            JSONObject day = schedule.getJSONObject(j);
                            if (day.getString("date").equals(date)) {
                                log("Appointments for Doctor " + doctorId + " on " + date + ":");
                                log(day.getJSONArray("appointments").toString(2));
                            }
                        }
                    }
                }
                if (!doctorFound) {
                    log("Error: Doctor with ID " + doctorId + " not found.");
                }
            } else {
                log("Invalid argument. Use 'ALL', 'GENERAL', 'PEDIATRICS', 'SURGERY', or 'DOCTOR <Doctor_ID>'.");
            }
        } catch (IOException e) {
            log("Error reading doctors file: " + e.getMessage());
        }
    }


    private static void handleBooking(String[] args) {
        if (args.length < 5) {
            log("Usage: book <patient.json> <doctor.json> <PATIENT ID> <DEPARTMENT>");
            return;
        }

        String patientsFile = args[1];
        String doctorsFile = args[2];
        String patientId = args[3];
        String department = args[4].toUpperCase();


        try {
            JSONArray patients = new JSONArray(new String(Files.readAllBytes(Paths.get(patientsFile))));
            boolean patientExists = false;

            for (int i = 0; i < patients.length(); i++) {
                if (patients.getJSONObject(i).getString("patient_id").equals(patientId)) {
                    patientExists = true;
                    break;
                }
            }

            if (!patientExists) {
                log("Error: Patient ID not found.");
                return;
            }

            JSONArray doctors = new JSONArray(new String(Files.readAllBytes(Paths.get(doctorsFile))));
            for (int i = 0; i < doctors.length(); i++) {
                JSONObject doctor = doctors.getJSONObject(i);
                if (doctor.getString("department").equalsIgnoreCase(department)) {
                    JSONArray schedule = doctor.getJSONArray("schedule");
                    LocalDate nextWorkingDay = getNextWorkingDay(LocalDate.now());

                    int appointmentDuration = getAppointmentDuration(department);

                    JSONObject targetDay = null;
                    for (int j = 0; j < schedule.length(); j++) {
                        JSONObject day = schedule.getJSONObject(j);
                        if (day.getString("date").equals(nextWorkingDay.toString())) {
                            targetDay = day;
                            break;
                        }
                    }

                    if (targetDay == null) {
                        // Create a new day entry if it doesn't exist
                        targetDay = new JSONObject();
                        targetDay.put("date", nextWorkingDay.toString());
                        targetDay.put("appointments", new JSONArray());
                        schedule.put(targetDay);
                    }

                    JSONArray appointments = targetDay.getJSONArray("appointments");

                    if (appointments.length() < 10) {
                        String appointmentTime = calculateNextAvailableTime(appointments, appointmentDuration);

                        JSONObject appointment = new JSONObject();
                        appointment.put("appointment_id", UUID.randomUUID().toString());
                        appointment.put("patient_id", patientId);
                        appointment.put("time", appointmentTime);
                        appointments.put(appointment);

                        Files.write(Paths.get(doctorsFile), doctors.toString().getBytes());
                        log("Appointment successfully booked for " + nextWorkingDay + " at " + appointmentTime + "!");
                        return;
                    } else {
                        log("Error: No available slots on " + nextWorkingDay + ".");
                        return;
                    }
                }
            }

            log("Error: No available doctors in department " + department + ".");
        } catch (IOException e) {
            log("Error handling booking: " + e.getMessage());
        }
    }

    private static int getAppointmentDuration(String department) {
        return switch (department.toUpperCase()) {
            case "PEDIATRICS" -> 45;
            case "SURGERY" -> 60;
            default -> 30;
        };
    }

    private static String calculateNextAvailableTime(JSONArray appointments, int duration) {
        LocalTime startTime = LocalTime.of(9, 0);

        for (int i = 0; i < appointments.length(); i++) {
            String time = appointments.getJSONObject(i).getString("time");
            LocalTime appointmentTime = LocalTime.parse(time);
            startTime = appointmentTime.plusMinutes(duration);
        }

        return startTime.toString();
    }

    private static LocalDate getNextWorkingDay(LocalDate date) {
        LocalDate nextDay = date.plusDays(1);
        while (nextDay.getDayOfWeek() == DayOfWeek.SATURDAY || nextDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }

    private static void handleSearch(String[] args) {
        if (args.length != 3) {
            log("Usage: search <patients.json> <PATIENT ID>");
            return;
        }

        String patientsFile = args[1];
        String patientId = args[2];

        try {
            JSONArray patients = new JSONArray(new String(Files.readAllBytes(Paths.get(patientsFile))));
            for (int i = 0; i < patients.length(); i++) {
                JSONObject patient = patients.getJSONObject(i);
                if (patient.getString("patient_id").equals(patientId)) {
                    log("Appointments for Patient " + patientId + ":");
                    log(patient.getJSONArray("appointments").toString(2));
                    return;
                }
            }

            log("Error: Patient ID not found.");
        } catch (IOException e) {
            log("Error reading patients file: " + e.getMessage());
        }
    }

    private static void handleCancel(String[] args) {
        if (args.length < 3) {
            log("Usage: cancel <doctors.json> <APPOINTMENT ID>");
            return;
        }

        String doctorsFile = args[1];
        String appointmentId = args[2];

        try {
            JSONArray doctors = new JSONArray(new String(Files.readAllBytes(Paths.get(doctorsFile))));
            for (int i = 0; i < doctors.length(); i++) {
                JSONObject doctor = doctors.getJSONObject(i);
                JSONArray schedule = doctor.getJSONArray("schedule");
                for (int j = 0; j < schedule.length(); j++) {
                    JSONArray appointments = schedule.getJSONObject(j).getJSONArray("appointments");
                    for (int k = 0; k < appointments.length(); k++) {
                        if (appointments.getJSONObject(k).getString("appointment_id").equals(appointmentId)) {
                            appointments.remove(k);
                            Files.write(Paths.get(doctorsFile), doctors.toString().getBytes());
                            log("Appointment " + appointmentId + " cancelled.");
                            return;
                        }
                    }
                }
            }

            log("Error: Appointment ID not found.");
        } catch (IOException e) {
            log("Error handling cancellation: " + e.getMessage());
        }
    }

    private static void handleOverdue(String[] args) {
        if(args.length != 3){
            log("Usage: overdue <patients.json> --admin");
            return;
        }
        if(!args[args.length - 1].equals("--admin")){
            log("Error: Unauthorized access. This command is available only to administrators.");
            return;
        }
        String patientsFile = args[1];

        try {
            JSONArray patients = new JSONArray(new String(Files.readAllBytes(Paths.get(patientsFile))));
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (int i = 0; i < patients.length(); i++) {
                JSONObject patient = patients.getJSONObject(i);
                JSONArray appointments = patient.getJSONArray("appointments");
                for (int j = 0; j < appointments.length(); j++) {
                    LocalDate appointmentDate = LocalDate.parse(appointments.getJSONObject(j).getString("date"), formatter);
                    if (appointmentDate.isBefore(today)){
                        if (appointments.getJSONObject(j).getString("status").equals("Scheduled")) {
                        log("Overdue: " + patient.getString("patient_id") + " - " + patient.getString("name"));
                        }
                    }
                }
            }
        } catch (IOException e) {
            log("Error reading patients file: " + e.getMessage());
        }
    }
}
