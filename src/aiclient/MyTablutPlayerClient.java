package aiclient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import aigenetics.*;
import ailogicbusiness.*;
import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class MyTablutPlayerClient extends TablutClient{

 private MyAIPlayerLogic aiLogic;
 private int timeoutSeconds;
 private State.Turn myRole; 
 private Set<String> history;
 
 public MyTablutPlayerClient(String role, int timeout, String serverIP) throws UnknownHostException, IOException{
	 super(role, "N-CODERS", timeout, serverIP);
     this.timeoutSeconds = timeout;   
     this.myRole = State.Turn.valueOf(role.toUpperCase());
     String weightsFile = (this.myRole == State.Turn.WHITE) ? "white_weights.dat" : "black_weights.dat";
     HeuristicWeights weights = loadWeights(weightsFile);
     this.history = new HashSet<String>();

     this.aiLogic = new MyAIPlayerLogic(weights, this.myRole);
     
     System.out.println("Player " + role + " inizializzato con pesi da " + weightsFile);
 }

 public void run() {

		try {
			this.declareName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("You are player " + this.getPlayer().toString() + "!");

		while (true) {
			try {
				this.read();
			} catch (ClassNotFoundException | IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
			State current = this.getCurrentState();
			if (this.getPlayer().equals(Turn.WHITE)) {
				if (current.getTurn().equals(StateTablut.Turn.WHITE)) {
					history.add(current.boardString());
					Action action=null;
					try {
						action = aiLogic.findBestMove(current, timeoutSeconds, -1, history);
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("Mossa scelta: " + action.toString());
					try {
						this.write(action);
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
					}
				}
				else if (current.getTurn().equals(StateTablut.Turn.BLACK)) {
					System.out.println("Waiting for your opponent move... ");
				}
				else if (current.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
					System.out.println("YOU WIN!");
					System.exit(0);
				}
				else if (current.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
					System.out.println("YOU LOSE!");
					System.exit(0);
				}
				else if (current.getTurn().equals(StateTablut.Turn.DRAW)) {
					System.out.println("DRAW!");
					System.exit(0);
				}

			} else {

				if (current.getTurn().equals(StateTablut.Turn.BLACK)) {
					history.add(current.boardString());
					Action action = null;
					try {
						action = aiLogic.findBestMove(current, timeoutSeconds, -1, history);
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("Mossa scelta: " + action.toString());
					try {
						this.write(action);
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
					}

				}

				else if (current.getTurn().equals(StateTablut.Turn.WHITE)) {
					System.out.println("Waiting for your opponent move... ");
				} else if (current.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
					System.out.println("YOU LOSE!");
					System.exit(0);
				} else if (current.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
					System.out.println("YOU WIN!");
					System.exit(0);
				} else if (current.getTurn().equals(StateTablut.Turn.DRAW)) {
					System.out.println("DRAW!");
					System.exit(0);
				}
			}
		}
 }


 private HeuristicWeights loadWeights(String filename) {
     try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
         return (HeuristicWeights) ois.readObject();
     } catch (Exception e) {
         System.err.println("ATTENZIONE: File dei pesi '" + filename + "' non trovato o corrotto.");
         System.err.println("Utilizzo pesi di default!");
         e.printStackTrace();
         double defaultWeights[];
         if (this.myRole == State.Turn.WHITE) {
             double W_Weights[] = {
            		 5.862447035296238, 7.2450964288789095, 6.334763673473421, 3.716889005439843,
            		  5.88913968883452, 1.6914359208350085, 6.916055395330703, -8.931451437339543,
            		  -6.569205028528853, 7.586997336261729, 6.329723336435293, 1.6316654824382542,
            		  -7.943763614768617, 4.761914795174438, 0.9941557532393932, -1.9467693204862937,
            		  10.230310434601632, 4.194727489490537, 5.292825244255267	 
             }; 
             defaultWeights=W_Weights;
         } else {
        	 double B_Weights[] = {
        			 5.207263889269218, 3.0746325809945545, 8.620200061485779, 4.753067256889794, 
        			  2.811834954515407, -7.701057393824493, -0.11736585123405441, 2.9336964837571458, 
        			  1.3232981694685528, 5.125096471837965, 9.085656895039985, -6.786838605882705, 
        			  5.656789696735774, 8.523081771061205, 0.793426696755971, 2.911364073990173, 
        			  9.071399978563205, 6.170528944743789, 5.391604742627568 
             }; 
             defaultWeights=B_Weights;            		 
         }
         return new HeuristicWeights(defaultWeights);
     }
 }


 public static void main(String[] args) throws UnknownHostException, IOException {
	 if (args.length!=3) {
		 System.out.println("usage: role[White/Black] Timeout ipAddress");
		 return;
	 }
     String role = args[0]; // "WHITE" o "BLACK"
     int timeout = Integer.parseInt(args[1]); // timeout
     String serverIP = args[2];
     
     MyTablutPlayerClient client = new MyTablutPlayerClient(role, timeout, serverIP);
     client.run();
 }
}