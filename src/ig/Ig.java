package ig;

import fg.*;
import nasm.*;
import util.graph.*;
import util.intset.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Ig {
	//Graphe d'interférence
    public Graph graph;
    //Ensembles in/out
    public FgSolution fgs;
    //Nombre de registres fictifs
    public int regNb;
    //Pré-nasm
    public Nasm nasm;
    //Accès sommet avec identifiant (entier)
    public Node int2Node[];
    //Graphe couleur
    public ColorGraph grapheCouleur;

    public Ig(FgSolution fgs) {
        this.fgs = fgs;
        this.graph = new Graph();
        this.nasm = fgs.nasm;
        this.regNb = this.nasm.getTempCounter();
        this.int2Node = new Node[regNb];
        this.construction();
    }

    public void construction() {
		//Pour chaque registre fictif
		for (int i = 0; i < regNb; i++)
			//Ajout d'un sommet au graphe d'interférence
			int2Node[i] = graph.newNode();
        //Pour chaque instruction nasm
		for (NasmInst inst : nasm.listeInst)
            //Récupération des ensembles in/out de l'instruction
		    for (IntSet ensemble : Arrays.asList(fgs.in.get(inst), fgs.out.get(inst)))
			    //Génération des arcs pour l'ensemble
			    genererArcsEnsemble(ensemble);
    }

    //Obtenir la précoloration de chaque registre
    public int[] getPrecoloredTemporaries() {
        //Tableau d'entiers couleur de dimension regNB
        int[] couleur = new int[regNb];
        //Pour chaque instruction nasm
        for (NasmInst instruction : nasm.listeInst)
            //Récupération des opérandes source et destination de l'instruction
            for (NasmOperand operande : Arrays.asList(instruction.source, instruction.destination))
                //Pour chaque registre
                for (NasmRegister registre : extraireRegistres(operande))
                    //Si c'est un registre général
                    if (registre.isGeneralRegister())
                        //Mise à jour de la couleur dans le tableau pour le registre
                        couleur[registre.val] = registre.color;
        //Retour du tableau
        return couleur;
    }

    //Associer à chaque registre fictif du pré-assembleur un registre réel
    public void allocateRegisters() {
        //Initialisation du graphe à colorier
        grapheCouleur = new ColorGraph(graph, 4, getPrecoloredTemporaries());
        //Coloration du graphe
        grapheCouleur.coloration();
        //Pour chaque instruction nasm
        for (NasmInst instruction : nasm.listeInst)
            //Récupération des opérandes source et destination de l'instruction
            for (NasmOperand operande : Arrays.asList(instruction.source, instruction.destination))
                //Pour chaque registre contenu dans l'opérande
                for (NasmRegister registre : extraireRegistres(operande))
                    //Si c'est un registre général et que sa couleur n'est pas définie
                    if (registre.isGeneralRegister() && registre.color == Nasm.REG_UNK)
                        //Coloration du registre avec la couleur trouvée dans le tableau grâce à son identifiant
                        registre.colorRegister(grapheCouleur.couleur[registre.val]);
    }

    //Récupérer les registres selon l'instance de l'opérande
    private ArrayList<NasmRegister> extraireRegistres(NasmOperand operande) {
        //Instanciation d'une liste de registre
        ArrayList<NasmRegister> registres = new ArrayList<>();
        //L'opérande est un registre, ajout à la liste
        if (operande instanceof NasmRegister) registres.add((NasmRegister)operande);
            //L'opérande est une adresse
        else if (operande instanceof NasmAddress) {
            //Récupération de l'adresse
            NasmAddress address = (NasmAddress) operande;
            //La base de l'adresse est un registre, ajout à la liste
            if (address.base instanceof NasmRegister) registres.add((NasmRegister)address.base);
            //Le déplacement de l'adresse est un registre, ajout à la liste
            if (address.offset instanceof NasmRegister) registres.add((NasmRegister)address.offset);
        }
        //On retourne la liste des registres
        return registres;
    }

    //Génération des arcs pour un ensemble donné
    private void genererArcsEnsemble(IntSet intSet) {
        //Pour chaque numéro de registre fictif
        for (int registre = 0; registre < regNb - 1; registre++)
            //Si le numéro de registre est présent dans l'ensemble
            if (intSet.isMember(registre))
                //Pour chaque numéro de registre prime fictif
                for (int registre_prime = 0; registre_prime < regNb - 1; registre_prime++)
                    //Si le numéro de registre est présent dans l'ensemble et est différent de celui de registre
                    if (intSet.isMember(registre_prime) && registre_prime != registre)
                        //Ajout d'un arc bidirectionnel dans le graphe entre les deux numéros de registre
                        graph.addNOEdge(int2Node[registre], int2Node[registre_prime]);
    }

    public void affiche(String baseFileName) {
        String fileName;
        PrintStream out = System.out;

        if (baseFileName != null) {
            try {
                baseFileName = baseFileName;
                fileName = baseFileName + ".ig";
                out = new PrintStream(fileName);
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        for (int i = 0; i < regNb; i++) {
            Node n = this.int2Node[i];
            out.print(n + " : ( ");
            for (NodeList q = n.succ(); q != null; q = q.tail) {
                out.print(q.head.toString());
                out.print(" ");
            }
            out.println(")");
        }
    }
}