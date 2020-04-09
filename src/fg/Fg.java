package fg;

import nasm.*;
import util.graph.*;

import java.util.*;
import java.io.*;

public class Fg implements NasmVisitor<Void> {
    public Nasm nasm;
    public Graph graph;
    Map<NasmInst, Node> inst2Node;
    Map<Node, NasmInst> node2Inst;
    Map<String, NasmInst> label2Inst;

    //Liste des éléments à ajouter au graphe
    private enum GrapheElement {
        SOMMET, ARCS
    }

    //Type d'élément
    private GrapheElement element;

    //Constructeur
    public Fg(Nasm nasm) {
        this.nasm = nasm;
        this.inst2Node = new HashMap<>();
        this.node2Inst = new HashMap<>();
        this.label2Inst = new HashMap<>();
        this.graph = new Graph();
        //Initialisation
        init();
    }

    //Initialisation du graphe d'analyse
    private void init() {
        //On commence par l'ajout des sommets
        element = GrapheElement.SOMMET;
        //Pour chaque instruction nasm
        for(NasmInst instruction : nasm.listeInst)
            //Parcours de l'instruction
            instruction.accept(this);
        //On termine par l'ajout des arcs
        element = GrapheElement.ARCS;
        //Pour chaque instruction nasm
        for (NasmInst instruction : nasm.listeInst)
            //Parcours de l'instruction
            instruction.accept(this);
    }

    //Traitement de l'instruction
    private void traiterInstruction(NasmInst instruction) {
        //En fonction de la valeur de element
        switch (element) {
            //Traitement des sommets
            case SOMMET:
                ajoutSommetInstGraph(instruction);
                break;
            //Traitement des arcs
            case ARCS:
                ajoutArcsInstGraph(instruction);
                break;
        }
    }

    //Ajouter un sommet pour une instruction nasm au graphe
    private void ajoutSommetInstGraph(NasmInst instruction) {
        //Ajout d'un sommet au graphe
        Node sommet = graph.newNode();
        //Liaison de l'instruction avec le sommet créé
        inst2Node.put(instruction,sommet);
        //Liaison du sommet avec l'instruction
        node2Inst.put(sommet,instruction);
        //Label de l'instruction existant
        if(instruction.label != null)
            //Liaison du label avec l'instruction
            label2Inst.put(instruction.label.toString(),instruction);
    }

    //Ajout les arcs liés à l'instruction au graphe
    private void ajoutArcsInstGraph(NasmInst instruction) {
        //Nom du type d'instruction
        String typeInstruction = instruction.getClass().getSimpleName();
        //Opération à effectuer
        switch (typeInstruction) {
            case "NasmJe":
            case "NasmJl":
            case "NasmJg":
            case "NasmJle":
            case "NasmJne":
            case "NasmJge":
                ajoutArcLabel(instruction);
                ajoutArcInstructionSuivante(instruction);
                break;
            case "NasmJmp":
            case "NasmCall":
                ajoutArcLabel(instruction);
                break;
            default:
                ajoutArcInstructionSuivante(instruction);
                break;
        }
    }

    //Ajout d'un arc vers l'instruction liée au label
    private void ajoutArcLabel(NasmInst instruction) {
        //Sommet instruction
        Node sommet = inst2Node.get(instruction);
        //Nom du label
        String label = instruction.address.toString();
        //Instruction du label
        NasmInst labelInst = label2Inst.get(label);
        //Sommet de l'instruction du label
        Node sommetLabel = inst2Node.get(labelInst);
        //Sommet non null
        if(sommetLabel != null)
            //Ajout de l'arc dans le graphe
            graph.addEdge(sommet,sommetLabel);
    }

    //Ajouter un arc vers l'instruction suivante
    private void ajoutArcInstructionSuivante(NasmInst instruction) {
        //Indice de l'instruction
        int indice = nasm.listeInst.indexOf(instruction);
        //Vérification position indice
        if (indice + 1 < nasm.listeInst.size()-1) {
            //Instruction suivante
            NasmInst instructionSuivante = nasm.listeInst.get(indice + 1);
            //Sommet de l'instruction
            Node sommet = inst2Node.get(instruction);
            //Sommet de l'instruction suivante
            Node sommetSuivant = inst2Node.get(instructionSuivante);
            //Ajout de l'arc dans le graphe
            graph.addEdge(sommet,sommetSuivant);
        }
    }

    @Override
    public Void visit(NasmAdd inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmCall inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmDiv inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmJe inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmJle inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmJne inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmMul inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmOr inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmCmp inst) {
        traiterInstruction(inst);
		return null;
    }

    public Void visit(NasmInst inst) {
        traiterInstruction(inst);
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmJge inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmJl inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmNot inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmPop inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmRet inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmXor inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmAnd inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmJg inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmJmp inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmMov inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmPush inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmSub inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmEmpty inst) {
        traiterInstruction(inst);
		return null;
    }

    @Override
    public Void visit(NasmAddress operand) {
		return null;
    }

    @Override
    public Void visit(NasmConstant operand) {
		return null;
    }

    @Override
    public Void visit(NasmLabel operand) {
		return null;
    }

    @Override
    public Void visit(NasmRegister operand) {
		return null;
    }

    //Affichage du graphe
    public void affiche(String baseFileName) {
        String fileName;
        PrintStream out = System.out;

        if (baseFileName != null) {
            try {
                baseFileName = baseFileName;
                fileName = baseFileName + ".fg";
                out = new PrintStream(fileName);
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        for (NasmInst nasmInst : nasm.listeInst) {
            Node n = this.inst2Node.get(nasmInst);
            out.print(n + " : ( ");
            for (NodeList q = n.succ(); q != null; q = q.tail) {
                out.print(q.head.toString());
                out.print(" ");
            }
            out.println(")\t" + nasmInst);
        }
    }
}
