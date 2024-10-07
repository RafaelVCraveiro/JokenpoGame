import java.io.*;
import java.net.*;
import java.util.Scanner;

public class JokenpoClient {
    private static int playerScore = 0;
    private static int cpuScore = 0;
    private static int empate = 0;
   
    public static void main(String[] args) {
        Scanner entrada = new Scanner(System.in);
        System.out.println("Digite o servidor no qual dejesa conectar:");
        String SERVER_ADDRESS = entrada.nextLine();
        System.out.println("Digite a porta do servidor:");
        int SERVER_PORT = entrada.nextInt();
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conectado ao servidor Jokenpo.");
            System.out.println("Escolha o modo de jogo (CPU/Player): ");
            String mode = console.readLine();
            out.println(mode);

            if (mode.equals("CPU")) {
                playAgainstCPU(in, out, console);
            } else if (mode.equals("Player")) {
                System.out.println(in.readLine()); // Mensagem de espera ou oponente encontrado
                playAgainstPlayer(in, out, console);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void playAgainstCPU(BufferedReader in, PrintWriter out, BufferedReader console) throws IOException {
        while (true) {
           
            

            System.out.println("Escolha sua jogada (Pedra, Papel, Tesoura) ou 'sair' para sair do jogo: ");
            String move = console.readLine();

            // Verifica se o jogador quer sair do jogo
            if (move.equalsIgnoreCase("sair")) {
                System.out.println("Você saiu do jogo.");
                out.println(move); // Envia a mensagem de saída para o servidor
                break; // Sai do loop e encerra o jogo
            }

            out.println(move);
            String response = in.readLine();
            System.out.println(response);

            // Atualiza o placar com base no resultado da rodada
            if (response.contains("Voce venceu!")) {
                playerScore++;
            } else if (response.contains("Voce perdeu!")) {
                cpuScore++;
            }else if (response.contains("Empate!")){
                empate++;
            }
            System.out.println("Placar: " + playerScore + " Vitorias, " + cpuScore + " Derrotas, " + empate + " Empates");
        }
    }

    private static void playAgainstPlayer(BufferedReader in, PrintWriter out, BufferedReader console) throws IOException {
        while (true) {
            System.out.println(in.readLine()); // Mensagem "Sua vez!" ou "Oponente escolheu sua jogada."
            System.out.println("Escolha sua jogada (Pedra, Papel, Tesoura) ou 'sair' para sair do jogo: ");
            String move = console.readLine();
            out.println(move);
            if (move.equalsIgnoreCase("sair")) {
                System.out.println("Saindo do jogo...");
                break;
            }
            
            String response = in.readLine();
            String oponenteSaiu = "O oponente saiu do jogo.";
            System.out.println(response);
            String score = in.readLine();
            System.out.println(score);
        }
    }
}
