package fg;

import nasm.*;
import util.graph.Node;
import util.graph.NodeList;
import util.intset.*;
import java.io.*;
import java.util.*;

public class FgSolution implements NasmVisitor<Void> {
    int iterNum = 0;
    public Nasm nasm;
    Fg fg;
    public Map<NasmInst, IntSet> use;
    public Map<NasmInst, IntSet> def;
    public Map<NasmInst, IntSet> in;
    public Map<NasmInst, IntSet> out;

	//Partie initialisation
	private enum Operation {
        INIT_USE_DEF, INIT_IN_OUT, CALC_IN_OUT
	}

	private int total_sommets;

	//Booléen convergence
	private boolean egaux;

	//Type d'initialisation
	private Operation operation;

	//Constructeur
    public FgSolution(Nasm nasm, Fg fg) {
        this.nasm = nasm;
        this.fg = fg;
        this.use = new HashMap<>();
        this.def = new HashMap<>();
        this.in = new HashMap<>();
        this.out = new HashMap<>();
        //Récupération du nombre de sommets dans le graphe
        this.total_sommets = fg.graph.nodeCount();
        //Initialisation
		init();
    }

    //Initialisation
    private void init() {
		//Initialisation des ensembles use/def
		operation = Operation.INIT_USE_DEF;
		//Pour chaque instruction nasm
		for(NasmInst instruction : nasm.listeInst)
			//Parcours de l'instruction
			instruction.accept(this);

        //Initialisation des ensembles in/out
		operation = Operation.INIT_IN_OUT;
        //Pour chaque instruction nasm
        for(NasmInst instruction : nasm.listeInst)
            //Initialistion des ensembles in/out
            instruction.accept(this);

        //On passe au calcul des ensembles in/out
        operation = Operation.CALC_IN_OUT;

        do {
            //Au départ les ensembles in/in' et out/out' sont égaux
            egaux = true;
            //Incrémentation itérations effectuées pour le calcul
            iterNum++;
            //Pour chaque instruction dans la liste
            for (NasmInst instruction : nasm.listeInst)
                //Calcul des ensembles in/out
                instruction.accept(this);
        } while (!egaux);
	}

	//Traiter une instruction
	private void traiterInstruction(NasmInst instruction) {
		//En fonction de la valeur de element
		switch (operation) {
			//Initialisation des ensembles use/def
			case INIT_USE_DEF:
				initEnsemblesUseDef(instruction);
				break;
			//Initialisation des ensembles in/out
			case INIT_IN_OUT:
				initEnsemblesInOut(instruction);
				break;
            //Calcul des ensembles in/out
            case CALC_IN_OUT:
                calculEnsemblesInOut(instruction);
                break;
		}
	}

	//Initialisation des ensembles use/def pour une instruction
	private void initEnsemblesUseDef(NasmInst instruction) {
        //Initialisation des registres modifiés par l'instruction
		def.put(instruction,new IntSet(total_sommets));
		//Initialisation des registres utilisés par l'instruction
		use.put(instruction,new IntSet(total_sommets));
		//Ajout d'un registre
		ajoutNumerosRegistres(instruction);
	}

    //Initialisation des ensembles in/out pour une instruction
	private void initEnsemblesInOut(NasmInst instruction) {
        //Initialisation de l'ensemble in
        in.put(instruction,new IntSet(total_sommets));
        //Initialisation de l'ensemble out
        out.put(instruction,new IntSet(total_sommets));
    }

	//Exécution de l'algorithme du cours
	private void calculEnsemblesInOut(NasmInst instruction) {
        //Sauvegarde de la valeur de in
        IntSet in_debut = in.get(instruction);
        //Sauvegarde de la valeur de out
        IntSet out_debut = out.get(instruction);
        //Copie de l'ensemble out pour éviter modification
        IntSet copie_out = out.get(instruction).copy();
        //Copie de l'ensemble use pour éviter modification
        IntSet copie_use = use.get(instruction).copy();
        //in(s) = use(s) ∪ (out(s) − def(s))
        in.put(instruction, copie_use.union(copie_out.minus(def.get(instruction))));
        //Successeurs du sommet de l'instruction
        NodeList successeurs = fg.inst2Node.get(instruction).succ();
        //Tant qu'il reste des successeurs
        while(successeurs != null){
            //Récupération de la tête de liste
            Node head = successeurs.head;
            //Union du successeur à l'ensemble out
            out.get(instruction).union(in.get(fg.node2Inst.get(head)));
            //Passage au successeur suivant
            successeurs = successeurs.tail;
        }
        //Booléen vérifiant que l'ensemble in de départ vaut celui d'arrivée
        boolean in_egal = in_debut.equal(in.get(instruction));
        //Booléen vérifiant que l'ensemble out de départ vaut celui d'arrivée
        boolean out_egal = out_debut.equal(out.get(instruction));
        //Si egaux vaut vrai et que un des deux ensembles n'est pas égal
        if (egaux && (!in_egal || !out_egal))
            //On passe à faux jusqu'à la fin de l'itération
            egaux = false;
	}

	//Ajouter les numéros de registres utilisés pour l'instruction
	private void ajoutNumerosRegistres(NasmInst inst){
        //Registres utilisés et modifiés par l'instruction
        List<NasmOperand> registres = Arrays.asList(inst.source, inst.destination);
        //Pour chaque registre
        for (NasmOperand operand : registres) {
            //Vérification que c'est un registre et qu'il est général sinon on passe au suivant
            if (!verifierRegistre(operand)) continue;
            //Récupération du registre
            NasmRegister registre = ((NasmRegister) operand);
            //Instruction définit des registres
            if (inst.srcDef || inst.destDef)
                //Ajout du registre dans l'ensemble de ceux modifiés par l'instruction
                def.get(inst).add(registre.val);
            //Instruction utilise des registres
            if (inst.srcUse || inst.destUse)
                //Ajout du registre dans l'ensemble de ceux utilisés par l'instruction
                use.get(inst).add(registre.val);
        }
	}

	//Vérifier un registre
	private boolean verifierRegistre(NasmOperand registre) {
        //Vérification que c'est une instance NasmRegister et que c'est un registre général (de type EAX, EBX, etc...)
        return registre instanceof NasmRegister && registre.isGeneralRegister();
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

    @Override
    public Void visit(NasmInst inst) {
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

	public void affiche(String baseFileName) {
		String fileName;
		PrintStream out = System.out;

		if (baseFileName != null) {
			try {
				baseFileName = baseFileName;
				fileName = baseFileName + ".fgs";
				out = new PrintStream(fileName);
			} catch (IOException e) {
				System.err.println("Error: " + e.getMessage());
			}
		}

		out.println("iter num = " + iterNum);
		for (NasmInst nasmInst : this.nasm.listeInst) {
			out.println("use = " + this.use.get(nasmInst) + " def = " + this.def.get(nasmInst) + "\tin = " + this.in.get(nasmInst) + "\t \tout = " + this.out.get(nasmInst) + "\t \t" + nasmInst);
		}
	}
}
