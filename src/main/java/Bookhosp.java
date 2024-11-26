import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Bookhosp {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Error: No input file provided. Usage: Bookhosp <input-file> <option>");
            System.exit(1);
        }

        String inputFile = args[0];
        String option = args.length > 1 ? args[1] : null;

        File file = new File(inputFile);
        if (!file.exists() || !file.canRead()) {
            System.err.println("Error: Input file not found or cannot be read. Please provide a valid file.");
            System.exit(1);
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine().trim();
                processCommand(command);
            }
        } catch (IOException e) {
            System.err.println("Error reading the input file: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void processCommand(String command) {
        if (command.isEmpty()) return;

        String[] parts = command.split(" ");
        String mainCommand = parts[0].toLowerCase();

        switch (mainCommand) {
            case "help":
                help();
                break;
            case "display":
                display();
                break;
            case "book":
                book();
                break;
            case "search":
                search();
                break;
            case "cancel":
                cancel();
                break;
            case "overdue":
                overdue();
                break;
            case "reschedule":
                reschedule();
                break;
            case "add":
                add();
                break;
            case "modify":
                modify();
                break;
            case "remove":
                remove();
                break;
            case "view-schedule":
                viewSchedule();
                break;
            default:
                System.out.println("Error: Unknown command '" + mainCommand + "'. Use 'help' to see available commands.");
                break;
        }
    }

    private static void help() {
        System.out.println("Available commands:");
        System.out.println("1. help - Displays this help message.");
        System.out.println("2. display <ARGUMENT> - Displays appointment slots or doctor schedules.");
        System.out.println("   ARGUMENT options: ALL, GENERAL, PEDIATRICS, SURGERY, DOCTOR <Doctor_ID>");
        System.out.println("3. book <Patient_ID> <Department> - Books an appointment for a patient.");
        System.out.println("4. search <Patient_ID> - Displays appointment details for a specific patient.");
        System.out.println("5. cancel <Patient_ID> - Cancels a patient's appointment.");
        System.out.println("6. overdue - (Admin Only) Displays overdue patients.");
        System.out.println("7. reschedule <Patient_ID> <Appointment_ID> <Department> - Reschedules an appointment.");
        System.out.println("8. add/modify/delete - Admin actions for records.");
        System.out.println("9. view schedule <ARGUMENT> - Displays schedules by doctor or department.");
    }

    private static void display() {
    }

    private static void book() {

    }

    private static void search(){

    }

    private static void cancel(){

    }

    private static void overdue(){

    }

    private static void reschedule() {
    }

    private static void add(){

    }

    private static void remove(){

    }

    private static void modify(){

    }

    private static void viewSchedule(){

    }


}
