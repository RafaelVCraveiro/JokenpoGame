import java.io.*;
import java.net.*;
import java.util.*;

public class JokenpoServer {
    
    private static List<ClientHandler> availableClients = new ArrayList<>();
    private static List<ClientHandler> activeClients = new ArrayList<>();
    

    public static void main(String[] args) throws UnknownHostException {
        Scanner entrada = new Scanner(System.in);
        String SERVER_IP = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Digite a porta na qual o servidor devera rodar:");
            int PORT = entrada.nextInt();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado no ip:" + SERVER_IP);
            System.out.println("Servidor Jokenpo iniciado na porta " + PORT);
            
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
                System.out.println("conectado");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private ClientHandler opponent;
        private static final String[] MOVES = {"Pedra", "Papel", "Tesoura"};
        private int wins = 0;
        private int losses = 0;
        private int ties = 0;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String mode = in.readLine();
                if (mode.equals("CPU")) {
                    playAgainstCPU();
                } else if (mode.equals("Player")) {
                    waitForOpponent();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void playAgainstCPU() {
            try {
                while (true) {
                    String playerMove = in.readLine();
                    if (playerMove == null) break;
                    if (playerMove.equalsIgnoreCase("sair")) {
                    out.println("Voce saiu do jogo.");
                    availableClients.remove(this);
                    
                    break;
                    }
                    String cpuMove = MOVES[new Random().nextInt(MOVES.length)];
                    String result = determineWinner(playerMove, cpuMove);
                    out.println("CPU escolheu: " + cpuMove + ". " + result);
                    
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void waitForOpponent() {
    synchronized (availableClients) {
        availableClients.add(this); // Adiciona o cliente atual à lista de clientes disponíveis

        for (ClientHandler client : availableClients) {
            if (client != this && client.opponent == null) {
                this.opponent = client;
                client.opponent = this;
                availableClients.remove(client); // Remove o cliente emparelhado da lista de clientes disponíveis
                activeClients.add(client);
                break;
            }
        }
    }

    if (opponent == null) {
        out.println("Aguardando um oponente...");
        return;
    }

    out.println("Oponente encontrado. Iniciando o jogo...");
    opponent.out.println("Oponente encontrado. Iniciando o jogo...");
    playAgainstPlayer();
}

        private void playAgainstPlayer() {
    try {
        while (true) {
            out.println("");
            String playerMove = in.readLine();
            String oponenteSaiu = "O oponente saiu do jogo";
            // Verifica se o jogador quer sair do jogo
            if (playerMove.equalsIgnoreCase("sair")) {
                out.println("Voce saiu do jogo.");
                opponent.out.println(oponenteSaiu);
                availableClients.remove(this);
                availableClients.remove(opponent);
                break;
            }

            opponent.out.println("");
            String opponentMove = opponent.in.readLine();
            
            // Verifica se o oponente quer sair do jogo
            if (opponentMove.equalsIgnoreCase("sair")) {
                
                out.println(oponenteSaiu);
                opponent.out.println("Voce saiu do jogo.");
                availableClients.remove(this);
                availableClients.remove(opponent);
                break;
                
            }
                    String result = determineWinner(playerMove, opponentMove);
                    String opponentResult = determineWinner(opponentMove, playerMove);

                    updateScore(result);
                    opponent.updateScore(opponentResult);

                    out.println("Oponente escolheu: " + opponentMove + ". " + result);
                    opponent.out.println("Oponente escolheu: " + playerMove + ". " + opponentResult);

                    out.println(getScore());
                    opponent.out.println(opponent.getScore());
        }
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        synchronized (availableClients) {
            activeClients.remove(this);
            activeClients.remove(opponent);
            availableClients.notifyAll();
        }
    }
}
        private void updateScore(String result) {
            switch (result) {
                case "Voce venceu!":
                    wins++;
                    break;
                case "Voce perdeu!":
                    losses++;
                    break;
                case "Empate!":
                    ties++;
                    break;
            }
        }

        private String getScore() {
            return "Placar: " + wins + " Vitorias, " + losses + " Derrotas, " + ties + " Empates";
        }

        private String determineWinner(String move1, String move2) {
            if (move1.equals(move2)) return "Empate!";
            switch (move1) {
                case "Pedra":
                    return (move2.equals("Tesoura")) ? "Voce venceu!" : "Voce perdeu!";
                case "Papel":
                    return (move2.equals("Pedra")) ? "Voce venceu!" : "Voce perdeu!";
                case "Tesoura":
                    return (move2.equals("Papel")) ? "Voce venceu!" : "Voce perdeu!";
            }
            return "Movimento invalido!";
        }
    }
}