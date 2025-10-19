package org.stackspotapi;

import java.util.Scanner;
import org.stackspotapi.service.AiChatService;

public class Main {

    private static void printMenu() {
        System.out.println("\n\n-------------------------------------------------");
        System.out.println("1) Mandar Pergunta.");
        System.out.println("0) Encerrar Processo.");
        System.out.print("Resposta: ");
        System.out.println("\n-------------------------------------------------");
    }

    private static int getValidOption(Scanner scanner) {
        while (true) {
            if (scanner.hasNextInt()) {
                int option = scanner.nextInt();
                if (option == 1 || option == 0) {
                    return option;
                }
            } else {
                scanner.next();
            }
            System.out.println("\nOpção inválida! Por favor, digite 1 ou 0.");
            printMenu();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int option;
        String prompt;

        while (true) {
            printMenu();
            option = getValidOption(scanner);

            if (option == 0) {
                System.out.println("\nEncerrando o processo. Até logo!");
                break;
            }

            scanner.nextLine();

            System.out.print("Digite sua pergunta: ");
            prompt = scanner.nextLine();

            if (prompt.isBlank()) {
                System.out.println("A pergunta não pode ser vazia. Tente novamente.");
                continue;
            }

            System.out.println("\nProcessando sua pergunta...");
            String response = AiChatService.ask(prompt);

            System.out.println("-------------------------------------------------");
            if (response != null) {
                System.out.println("Resposta Final da IA:");
                System.out.println(response);
            } else {
                System.err.println("Não foi possível obter uma resposta da IA.");
            }
        }

        scanner.close();
    }
}
