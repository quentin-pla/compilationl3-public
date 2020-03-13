package sa;

import c3a.*;
import ts.Ts;
import ts.TsItemFct;
import ts.TsItemVar;

public class Sa2c3a extends SaDepthFirstVisitor<C3aOperand> {
    private C3a c3a;
    private Ts table;

    //Constructeur
    public Sa2c3a(SaNode root, Ts table) {
        this.table = table;
        c3a = new C3a();
        root.accept(this);
    }

    //Getter c3a
    public C3a getC3a() {
        return c3a;
    }

    //Déclaration Programme
    @Override
    public C3aOperand visit(SaProg node) {
        //Parcours toutes les fonctions
        node.getFonctions().accept(this);
        return null;
    }

    //Déclaration d'une fonction
    @Override
    public C3aOperand visit(SaDecFonc node) {
        //Récupération de la fonction à partir de la table
        TsItemFct fonction = this.table.getFct(node.getNom());

        //Entrée fonction
        C3aInstFBegin begin = new C3aInstFBegin(fonction, "entree fonction");
        //Ajout instruction début fonction
        c3a.ajouteInst(begin);

        //Parcours toutes les instructions du corps de la fonction
        node.getCorps().accept(this);

        //Sortie fonction
        C3aInstFEnd end = new C3aInstFEnd("");
        //Ajout instruction fin fonction
        c3a.ajouteInst(end);

        return null;
    }

    //Déclaration d'une variable
    @Override
    public C3aOperand visit(SaDecVar node) {
        //Récupération de la variable
        TsItemVar var = table.getVar(node.getNom());

        //Instanciation d'une nouvelle variable c3a
        return new C3aVar(var, null);
    }

    //Appel d'une fonction
    @Override
    public C3aOperand visit(SaAppel node) {
        //Récupération des arguments de la fonction
        SaLExp arguments = node.getArguments();
        C3aOperand arg;
        //Pour chaque argument
        while (arguments != null) {
            //Récupération du argument en début de liste
            arg = arguments.getTete().accept(this);
            //Instanciation d'un nouveau argument c3a
            C3aInstParam instarg = new C3aInstParam(arg,"");
            //Ajout de l'instance à la liste
            c3a.ajouteInst(instarg);
            //Passage au paramètre suivant
            arguments = arguments.getQueue();
        }

        //Instanciation d'une fonction c3a
        C3aFunction fonction = new C3aFunction(table.getFct(node.getNom()));

        //Instanciation d'une variable temporaire
        C3aTemp result = c3a.newTemp();

        //Instanciation d'une instruction appel c3a
        C3aInstCall instAppel = new C3aInstCall(fonction, result, "");
        //Ajout à la liste
        c3a.ajouteInst(instAppel);

        //Retour du résultat de l'appel
        return result;
    }

    //Référence
    @Override
    public C3aOperand visit(SaVarSimple node) {
        //Récupération de la référence
        TsItemVar var = table.getVar(node.getNom());

        //Retourne nouvelle variable
        return new C3aVar(var, null);
    }

    //Variable indicée
    @Override
    public C3aOperand visit(SaVarIndicee node) {
        //Récupération de la variable
        TsItemVar var = table.getVar(node.getNom());

        //Récupération de l'indice
        C3aOperand indice = node.getIndice().accept(this);

        //Retourne nouvelle variable
        return new C3aVar(var, indice);
    }

    /******EXPRESSIONS******/

    //Addition
    @Override
    public C3aOperand visit(SaExpAdd node) {
        //Récupération de la première opérande
        C3aOperand op1 = node.getOp1().accept(this);
        //Récupération de la deuxième opérande
        C3aOperand op2 = node.getOp2().accept(this);

        //Création d'une variable temporaire contenant le résultat
        C3aOperand result = c3a.newTemp();

        //Création d'une instruction de type addition
        C3aInstAdd add = new C3aInstAdd(op1, op2, result, "");

        //Ajout de l'instruction à la liste
        c3a.ajouteInst(add);

        //Retour du résultat
        return result;
    }

    //Soustraction
    @Override
    public C3aOperand visit(SaExpSub node) {
        //Récupération de la première opérande
        C3aOperand op1 = node.getOp1().accept(this);
        //Récupération de la deuxième opérande
        C3aOperand op2 = node.getOp2().accept(this);

        //Création d'une variable temporaire contenant le résultat
        C3aOperand result = c3a.newTemp();

        //Création d'une instruction de type soustraction
        C3aInstSub sub = new C3aInstSub(op1, op2, result, "");

        //Ajout de l'instruction à la liste
        c3a.ajouteInst(sub);

        return result;
    }

    //Multiplication
    @Override
    public C3aOperand visit(SaExpMult node) {
        //Récupération de la première opérande
		C3aOperand op1 = node.getOp1().accept(this);
        //Récupération de la deuxième opérande
		C3aOperand op2 = node.getOp2().accept(this);

        //Création d'une variable temporaire contenant le résultat
        C3aOperand result = c3a.newTemp();

        //Création d'une instruction de type multiplication
        C3aInstMult mult = new C3aInstMult(op1, op2, result, "");

        //Ajout de l'instruction à la liste
        c3a.ajouteInst(mult);

        return result;
    }

    //Division
    @Override
    public C3aOperand visit(SaExpDiv node) {
        //Récupération de la première opérande
		C3aOperand op1 = node.getOp1().accept(this);
        //Récupération de la deuxième opérande
		C3aOperand op2 = node.getOp2().accept(this);

        //Création d'une variable temporaire contenant le résultat
        C3aOperand result = c3a.newTemp();

        //Création d'une instruction de type division
        C3aInstDiv div = new C3aInstDiv(op1, op2, result, "");

        //Ajout de l'instruction à la liste
        c3a.ajouteInst(div);

        return result;
    }

    //Inférieur
    @Override
    public C3aOperand visit(SaExpInf node) {
        //Récupération de la première opérande
        C3aOperand op1 = node.getOp1().accept(this);
        //Récupération de la deuxième opérande
        C3aOperand op2 = node.getOp2().accept(this);

        //Label pour se diriger directement au retour de la fonction
        C3aLabel retour = c3a.newAutoLabel();

        //Création d'un booléen temporaire
        C3aOperand bool = c3a.newTemp();

        //Passage du booléen à vrai
        C3aInstAffect affect = new C3aInstAffect(c3a.True, bool, "");
        //Ajout de l'instruction
        c3a.ajouteInst(affect);

        //Si la première opérande est inférieur à la deuxième alors on le retourne
        C3aInstJumpIfLess inf = new C3aInstJumpIfLess(op1, op2, retour, "");
        //Ajout de l'instruction
        c3a.ajouteInst(inf);

        //Passage du booléen à faux car opérande 1 supérieure à la deuxième
        affect = new C3aInstAffect(c3a.False, bool, "");
        //Ajout de l'instruction
        c3a.ajouteInst(affect);

        //Ajout du label retour à l'instruction suivante
        c3a.addLabelToNextInst(retour);
        //Retour du booléen
        return bool;
    }

    //Egal
    @Override
    public C3aOperand visit(SaExpEqual node) {
        //Récupération de la première opérande
        C3aOperand op1 = node.getOp1().accept(this);
        //Récupération de la deuxième opérande
        C3aOperand op2 = node.getOp2().accept(this);

        //Label pour se diriger directement au retour de la fonction
        C3aLabel retour = c3a.newAutoLabel();

        //Création d'un booléen temporaire
        C3aOperand bool = c3a.newTemp();

        //Passage du booléen à vrai
        C3aInstAffect affect = new C3aInstAffect(c3a.True, bool, "");
        //Ajout de l'instruction
        c3a.ajouteInst(affect);

        //Si la première opérande est égale à la deuxième alors on le retourne
        C3aInstJumpIfEqual equal = new C3aInstJumpIfEqual(op1, op2, retour, "");
        //Ajout de l'instruction
        c3a.ajouteInst(equal);

        //Passage du booléen à faux car opérande 1 inégale à la deuxième
        affect = new C3aInstAffect(c3a.False, bool, "");
        //Ajout de l'instruction
        c3a.ajouteInst(affect);

        //Ajout du label retour à l'instruction suivante
        c3a.addLabelToNextInst(retour);
        //Retour du booléen
        return bool;
    }

    //Et
    @Override
    public C3aOperand visit(SaExpAnd node) {
        //Récupération de la première opérande
        C3aOperand op1 = node.getOp1().accept(this);
        //Récupération de la deuxième opérande
        C3aOperand op2 = node.getOp2().accept(this);

        //Label pour se diriger directement au retour de la fonction
        C3aLabel retour = c3a.newAutoLabel();

        //Label pour se diriger lorsque un/les test(s) est/sont faux
        C3aLabel invalide = c3a.newAutoLabel();

        //Création d'un booléen temporaire
        C3aOperand bool = c3a.newTemp();

        //Vérification du premier test s'il est faux ou pas
        C3aInstJumpIfEqual test1Faux = new C3aInstJumpIfEqual(op1, c3a.False, invalide, "");
        //Ajout de l'instruction
        c3a.ajouteInst(test1Faux);

        //Vérification du deuxième test s'il est faux ou pas
        C3aInstJumpIfEqual test2Faux = new C3aInstJumpIfEqual(op2, c3a.False, invalide, "");
        //Ajout de l'instruction
        c3a.ajouteInst(test2Faux);

        //Passage du booléen à vrai si les deux tests sont vrais
        C3aInstAffect affect = new C3aInstAffect(c3a.True, bool, "");
        //Ajout de l'instruction
        c3a.ajouteInst(affect);

        //Saut vers le retour de la fonction pour retourner le booléen
        C3aInstJump gotoRetour = new C3aInstJump(retour, "");
        //Ajout de l'instruction
        c3a.ajouteInst(gotoRetour);

        //Ajout du label invalide à l'instruction suivante
        c3a.addLabelToNextInst(invalide);
        //Passage du booléen à faux car un ou les tests sont invalides
        affect = new C3aInstAffect(c3a.False, bool, "");
        //Ajout de l'instruction
        c3a.ajouteInst(affect);

        //Ajout du label retour à l'instruction suivante
        c3a.addLabelToNextInst(retour);
        //Retour du booléen
        return bool;
    }

    //Ou
    @Override
    public C3aOperand visit(SaExpOr node) {
        //Récupération de la première opérande
        C3aOperand op1 = node.getOp1().accept(this);
        //Récupération de la deuxième opérande
        C3aOperand op2 = node.getOp2().accept(this);

        //Label pour se diriger directement au retour de la fonction
        C3aLabel retour = c3a.newAutoLabel();

        //Label pour se diriger lorsqu'au minimum un test est vrai
        C3aLabel valide = c3a.newAutoLabel();

        //Création d'un booléen temporaire
        C3aOperand bool = c3a.newTemp();

        //Vérification du premier test s'il est vrai ou pas
        C3aInstJumpIfNotEqual test1Vrai = new C3aInstJumpIfNotEqual(op1, c3a.False, valide, "");
        //Ajout de l'instruction
        c3a.ajouteInst(test1Vrai);

        //Vérification du deuxième test s'il est vrai ou pas
        C3aInstJumpIfNotEqual test2Vrai = new C3aInstJumpIfNotEqual(op2, c3a.False, valide, "");
        //Ajout de l'instruction
        c3a.ajouteInst(test2Vrai);

        //Passage du booléen à faux si les deux tests sont faux
        C3aInstAffect affect = new C3aInstAffect(c3a.False, bool, "");
        //Ajout de l'instruction
        c3a.ajouteInst(affect);

        //Saut vers le retour de la fonction pour retourner le booléen
        C3aInstJump gotoRetour = new C3aInstJump(retour, "");
        //Ajout de l'instruction
        c3a.ajouteInst(gotoRetour);

        //Ajout du label valide à l'instruction suivante
        c3a.addLabelToNextInst(valide);
        //Passage du booléen à vrai car un test est vrai
        affect = new C3aInstAffect(c3a.True, bool, "");
        //Ajout de l'instruction
        c3a.ajouteInst(affect);

        //Ajout du label retour à l'instruction suivante
        c3a.addLabelToNextInst(retour);
        //Retour du booléen
        return bool;
    }

    //Entier
    @Override
    public C3aOperand visit(SaExpInt node) {
        //Retour d'une constante de type entier
        return new C3aConstant(node.getVal());
    }

    //Appel expression
    @Override
    public C3aOperand visit(SaExpAppel node) {
        //Récupération de l'appel
        SaAppel val = node.getVal();

        //Retour de l'instruction
        return val.accept(this);
    }

    //Variable
    @Override
    public C3aOperand visit(SaExpVar node) {
        //Récupération de la variable
        SaVar var = node.getVar();

        //Retour de l'instruction
        return var.accept(this);
    }

    //Lecture
    @Override
    public C3aOperand visit(SaExpLire node) {
        //Parcours des instructions
        return node.accept(this);
    }

    //Non
    @Override
    public C3aOperand visit(SaExpNot node) {
        //Parcours des instructions
        node.getOp1().accept(this);
        return null;
    }

    /******INSTRUCTIONS******/

    //Ecriture
    @Override
    public C3aOperand visit(SaInstEcriture node) {
        //Récupération de l'argument
        C3aOperand exp = node.getArg().accept(this);

        //Ajout d'une nouvelle instruction d'écriture
        c3a.ajouteInst(new C3aInstWrite(exp, ""));

        return null;
    }

    //Tant que
    @Override
    public C3aOperand visit(SaInstTantQue node) {
        //Label pour retourner au test
        C3aLabel boucle = c3a.newAutoLabel();
        //Label pour terminer la boucle
        C3aLabel retour = c3a.newAutoLabel();

        //Ajout du label boucle à l'instruction suivante
        c3a.addLabelToNextInst(boucle);
        //Récupération du test
        C3aOperand test = node.getTest().accept(this);

        //Saut vers retour si test invalide
        C3aInstJumpIfEqual gotoRetour = new C3aInstJumpIfEqual(test, c3a.False, retour, "");
        //Ajout à la liste
        c3a.ajouteInst(gotoRetour);

        //Parcours des instructions à faire tant que le test est valide
        node.getFaire().accept(this);

        //Saut vers le test
        C3aInstJump jump = new C3aInstJump(boucle, "");
        //Ajout à la liste
        c3a.ajouteInst(jump);

        //Ajout du label retour à l'instruction suivante
        c3a.addLabelToNextInst(retour);
        return null;
    }

    //Affectation
    @Override
    public C3aOperand visit(SaInstAffect node) {
        //Récupération de la première opérande
        C3aOperand op1 = node.getRhs().accept(this);
        //Récupération de la deuxième opérande
        C3aOperand result = node.getLhs().accept(this);

        //Création d'une nouvelle instruction d'affectation
        C3aInstAffect aff = new C3aInstAffect(op1, result, "");
        //Ajout de l'instruction à la liste
        c3a.ajouteInst(aff);

        //Retourne résultat
        return result;
    }

    //Condition Si
    @Override
    public C3aOperand visit(SaInstSi node) {
        //Récupération du test
        C3aOperand test = node.getTest().accept(this);

        //Label lorsque le test n'est pas validé
        C3aLabel sinon = c3a.newAutoLabel();
        //Label lorsque le test est validé
        C3aLabel retour = c3a.newAutoLabel();

        //Saut vers sinon si test invalide
        C3aInstJumpIfEqual gotoSinon = new C3aInstJumpIfEqual(test, c3a.False, sinon, "");
        //Ajout à la liste
        c3a.ajouteInst(gotoSinon);

        //Parcours des instructions alors
        node.getAlors().accept(this);

        //Saut vers la fin
        C3aInstJump gotoRetour = new C3aInstJump(retour, "");
        //Ajout à la liste
        c3a.ajouteInst(gotoRetour);

        //Ajout du label sinon à l'instruction suivante
        c3a.addLabelToNextInst(sinon);
        //Application des instructions sinon
        node.getSinon().accept(this);

        //Ajout du label retour à l'instruction suivante
        c3a.addLabelToNextInst(retour);
        return null;
    }

    //Retour
    @Override
    public C3aOperand visit(SaInstRetour node) {
        //Récupération de l'instruction retour
        C3aOperand operand = node.getVal().accept(this);

        //Ajout de l'instruction
        c3a.ajouteInst(new C3aInstReturn(operand, ""));

        //Retour instruction
        return operand;
    }
}