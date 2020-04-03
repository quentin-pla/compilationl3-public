import c3a.*;
import nasm.*;
import ts.Ts;
import ts.TsItemFct;

public class C3a2nasm implements C3aVisitor<NasmOperand> {
    private C3a c3a;
    private Nasm nasm;
    private Ts tableGlobale;
    private TsItemFct currentFct;

    //Constructeur
    public C3a2nasm(C3a c3a, Ts tableGlobale) {
        this.c3a = c3a;
        this.tableGlobale = tableGlobale;
        this.nasm = new Nasm(tableGlobale);
        this.currentFct = null;

        //Création d'un nouveau registre
        NasmRegister reg_ebx = nasm.newRegister();
        //Registre utilisant la macro EBX
        reg_ebx.colorRegister(Nasm.REG_EBX);

        //Création d'un nouveau registre
        NasmRegister reg_eax = nasm.newRegister();
        //Registre utilisant la macro EAX
        reg_eax.colorRegister(Nasm.REG_EAX);

        //Label fonction main()
        NasmLabel label = new NasmLabel("main");
        //Ajout de l'instruction
        nasm.ajouteInst(new NasmCall(null, label, ""));

        //Valeur retour programme
        NasmOperand cst1 = new NasmConstant(0);
        //Association de la constante au registre ebx
        nasm.ajouteInst(new NasmMov(null, reg_ebx, cst1, ""));

        //Création d'une constante de valeur 1
        NasmOperand cst2 = new NasmConstant(1);
        //Association de la constante au registre eax
        nasm.ajouteInst(new NasmMov(null, reg_eax, cst2, ""));

        //Interruption
        nasm.ajouteInst(new NasmInt(null, ""));

        //Parcours des instructions pour chaque instruction C3A
        for (C3aInst inst: c3a.listeInst) {
            inst.accept(this);
        }
    }

    //Getter
    public Nasm getNasm() {
        return nasm;
    }

    //Récupération du label de l'instruction si existant
    private NasmOperand getLabel(C3aInst inst) {
        return (inst.label != null) ? inst.label.accept(this) : null;
    }

    //Début fonction
    @Override
    public NasmOperand visit(C3aInstFBegin inst) {
        //Récupération du label de la fonction
        NasmOperand label = (inst.val.identif != null) ? new NasmLabel(inst.val.identif) : null;

        //Création d'un nouveau registre
        NasmRegister reg_ebp = new NasmRegister(Nasm.REG_EBP);
        //Registre utilisant la macro EBP
        reg_ebp.colorRegister(Nasm.REG_EBP);
        //Sauvegarde de la valeur de ebp
        nasm.ajouteInst(new NasmPush(label, reg_ebp, ""));

        //Création d'un nouveau registre
        NasmRegister reg_esp = new NasmRegister(Nasm.REG_ESP);
        //Registre utilisant la macro ESP
        reg_esp.colorRegister(Nasm.REG_ESP);
        //Attribution d'une nouvelle valeur pour ebp
        nasm.ajouteInst(new NasmMov(null, reg_ebp, reg_esp, ""));

        //Récupération de la fonction
        currentFct = inst.val;

        //Récupération du nombre de variables
        int nombre_variables = currentFct.getTable().nbVar();

        //Instanciation d'une constante
        NasmConstant constante = new NasmConstant(nombre_variables*4);

        //Allocation des variables locales
        nasm.ajouteInst(new NasmSub(null,reg_esp,constante,""));

        return null;
    }

    //Fin fonction
    @Override
    public NasmOperand visit(C3aInstFEnd inst) {
        //Récupération du label si existant
        NasmOperand label = getLabel(inst);

        //Création d'un nouveau registre
        NasmRegister reg_ebp = new NasmRegister(Nasm.REG_EBP);
        //Registre utilisant la macro EBP
        reg_ebp.colorRegister(Nasm.REG_EBP);

        //Création d'un nouveau registre
        NasmRegister reg_esp = new NasmRegister(Nasm.REG_ESP);
        //Registre utilisant la macro ESP
        reg_esp.colorRegister(Nasm.REG_ESP);

        //Récupération du nombre de variables
        int nbVariables = currentFct.getTable().nbVar();

        //Instanciation d'une constante
        NasmConstant constante = new NasmConstant(nbVariables*4);

        //Désallocation des variables locales
        nasm.ajouteInst(new NasmAdd(label,reg_esp,constante,""));

        //Restauration de la valeur de ebp
        nasm.ajouteInst(new NasmPop(label, reg_ebp, ""));

        //Instruction fin de fonction
        nasm.ajouteInst(new NasmRet(null, ""));

        return null;
    }

    //Addition
    @Override
    public NasmOperand visit(C3aInstAdd inst) {
        //Récupération du label si existant
        NasmOperand label = getLabel(inst);

        //Récupération de la première opérande
        NasmOperand op1 = inst.op1.accept(this);
        //Récupération de la deuxième opérande
        NasmOperand op2 = inst.op2.accept(this);
        //Récupération du résultat
        NasmOperand resultat = inst.result.accept(this);

        //Assignation de la première opérande dans le résultat
        nasm.ajouteInst(new NasmMov(label, resultat, op1, ""));
        //Addition de la deuxième opérande au résultat
        nasm.ajouteInst(new NasmAdd(null, resultat, op2, ""));

        return null;
    }

    //Soustraction
    @Override
    public NasmOperand visit(C3aInstSub inst) {
        //Récupération du label si existant
        NasmOperand label = getLabel(inst);

        //Récupération de la première opérande
        NasmOperand op1 = inst.op1.accept(this);
        //Récupération de la deuxième opérande
        NasmOperand op2 = inst.op2.accept(this);
        //Récupération du résultat
        NasmOperand resultat = inst.result.accept(this);

        //Assignation de la première opérande dans le résultat
        nasm.ajouteInst(new NasmMov(label, resultat, op1, ""));
        //Soustraction de la deuxième opérande au résultat
        nasm.ajouteInst(new NasmSub(null, resultat, op2, ""));

        return null;
    }

    //Multiplication
    @Override
    public NasmOperand visit(C3aInstMult inst) {
        //Récupération du label si existant
        NasmOperand label = getLabel(inst);

        //Récupération de la première opérande
        NasmOperand op1 = inst.op1.accept(this);
        //Récupération de la deuxième opérande
        NasmOperand op2 = inst.op2.accept(this);
        //Récupération du résultat
        NasmOperand resultat = inst.result.accept(this);

        //Assignation de la première opérande dans le résultat
        nasm.ajouteInst(new NasmMov(label, resultat, op1, ""));
        //Multiplication de la deuxième opérande au résultat
        nasm.ajouteInst(new NasmMul(null, resultat, op2, ""));

        return null;
    }

    //Division
    @Override
    public NasmOperand visit(C3aInstDiv inst) {
        //Récupération du label si existant
        NasmOperand label = getLabel(inst);

        //Récupération de la première opérande
        NasmOperand op1 = inst.op1.accept(this);
        //Récupération de la deuxième opérande
        NasmOperand op2 = inst.op2.accept(this);
        //Récupération du résultat
        NasmOperand resultat = inst.result.accept(this);

        //Création d'un nouveau registre
        NasmRegister reg_eax = nasm.newRegister();
        //Assignation registre à la macro EAX
        reg_eax.colorRegister(Nasm.REG_EAX);

        //Création d'un nouveau registre
        NasmRegister reg_ebx = nasm.newRegister();
        //Assignation registre à la macro EBX
        reg_ebx.colorRegister(Nasm.REG_EBX);

        //Assignation de la première opérande dans le registre EAX
        nasm.ajouteInst(new NasmMov(label, reg_eax, op1, ""));
        //Assignation de la deuxième opérande dans le registre EBX
        nasm.ajouteInst(new NasmMov(label, reg_ebx, op2, ""));

        //Division dans le registre EBX
        nasm.ajouteInst(new NasmDiv(null, reg_ebx, ""));

        //Récupération du résultat
        nasm.ajouteInst(new NasmMov(null, resultat, reg_ebx, ""));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstCall inst) {
        //Récupération du label si existant
        NasmOperand label = getLabel(inst);

        //Création d'un nouveau registre
        NasmRegister reg_esp = nasm.newRegister();
        //Registre utilisant la macro ESP
        reg_esp.colorRegister(Nasm.REG_ESP);

        //Récupération du résultat
        NasmOperand resultat = inst.result.accept(this);

        //Allocation mémoire de 4 octets pour la valeur de retour
        nasm.ajouteInst(new NasmSub(label,reg_esp,new NasmConstant(4),""));

        //Appel de la fonction
        nasm.ajouteInst(new NasmCall(null,new NasmLabel(inst.op1.val.identif),""));

        //Récupération de la valeur de retour
        nasm.ajouteInst(new NasmPop(null, resultat,""));

        //Récupération du nombre d'arguments
        int nombre_arguments = inst.op1.val.getNbArgs();

        //Si des arguments sont passés en paramètres
        if (nombre_arguments > 0) {
            //Mémoire occupée occupée par les paramètres (un paramètre occupe 4 octets)
            NasmConstant memoire_occupee = new NasmConstant(nombre_arguments * 4);
            //Désallocation de l’espace occupé dans la pile par les paramètres
            nasm.ajouteInst(new NasmAdd(null, reg_esp,  memoire_occupee, ""));
        }

        return null;
    }

    @Override
    public NasmOperand visit(C3aInst inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aInstJump inst) {
        //Récupération du label si existant
        NasmOperand label = getLabel(inst);

        //Récupération du résultat
        NasmOperand resultat = inst.result.accept(this);

        //Ajout d'un saut vers le résultat
        nasm.ajouteInst(new NasmJmp(label, resultat,""));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstJumpIfEqual inst) {
        //Récupération du label si existant
        NasmOperand label = getLabel(inst);

        //Récupération de la première opérande
        NasmOperand op1 = inst.op1.accept(this);
        //Récupération de la deuxième opérande
        NasmOperand op2 = inst.op2.accept(this);
        //Récupération du résultat
        NasmOperand resultat = inst.result.accept(this);

        //Si la première opérande est un registre général (de type EAX, EBX, etc...)
        if (op1.isGeneralRegister())
            //Comparaison des opérandes
            nasm.ajouteInst(new NasmCmp(label,op1,op2,""));
        else {
            //Création d'un nouveau registre
            NasmRegister registre = nasm.newRegister();
            //Attribution de la valeur de la première opérande au registre
            nasm.ajouteInst(new NasmMov(label,registre,op1,""));
            //Comparaison du registre avec la deuxième opérande
            nasm.ajouteInst(new NasmCmp(null,registre,op2,""));
        }

        //Saut vers le résultat si égal
        nasm.ajouteInst(new NasmJe(null,resultat,""));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstJumpIfNotEqual inst) {
        //Récupération du label si existant
        NasmOperand label = getLabel(inst);

        //Récupération de la première opérande
        NasmOperand op1 = inst.op1.accept(this);
        //Récupération de la deuxième opérande
        NasmOperand op2 = inst.op2.accept(this);
        //Récupération du résultat
        NasmOperand resultat = inst.result.accept(this);

        //Si la première opérande est un registre général (de type EAX, EBX, etc...)
        if (op1.isGeneralRegister())
            //Comparaison des opérandes
            nasm.ajouteInst(new NasmCmp(label,op1,op2,""));
        else {
            //Création d'un nouveau registre
            NasmRegister registre = nasm.newRegister();
            //Attribution de la valeur de la première opérande au registre
            nasm.ajouteInst(new NasmMov(label,registre,op1,""));
            //Comparaison du registre avec la deuxième opérande
            nasm.ajouteInst(new NasmCmp(null,registre,op2,""));
        }

        //Saut vers le résultat si pas égal
        nasm.ajouteInst(new NasmJne(null,resultat,""));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstJumpIfLess inst) {
        //Récupération du label si existant
        NasmOperand label = getLabel(inst);

        //Récupération de la première opérande
        NasmOperand op1 = inst.op1.accept(this);
        //Récupération de la deuxième opérande
        NasmOperand op2 = inst.op2.accept(this);
        //Récupération du résultat
        NasmOperand resultat = inst.result.accept(this);

        //Si la première opérande est un registre général (de type EAX, EBX, etc...)
        if (op1.isGeneralRegister())
            //Comparaison des opérandes
            nasm.ajouteInst(new NasmCmp(label,op1,op2,""));
        else {
            //Création d'un nouveau registre
            NasmRegister registre = nasm.newRegister();
            //Attribution de la valeur de la première opérande au registre
            nasm.ajouteInst(new NasmMov(label,registre,op1,""));
            //Comparaison du registre avec la deuxième opérande
            nasm.ajouteInst(new NasmCmp(null,registre,op2,""));
        }

        //Saut vers le résultat si inférieur
        nasm.ajouteInst(new NasmJl(null,resultat,""));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstRead inst) {
        //Récupération du label si existant
        NasmOperand label = getLabel(inst);

        //Récupération du résultat
        NasmOperand resultat = inst.result.accept(this);

        //Création d'un nouveau registre
        NasmRegister reg_eax = nasm.newRegister();
        //Registre utilisant la macro EAX
        reg_eax.colorRegister(Nasm.REG_EAX);

        //Création d'un label sinput
        NasmOperand sinput = new NasmLabel("sinput");
        //Création d'un label readline
        NasmOperand realine = new NasmLabel("readline");
        //Création d'un label atoi
        NasmOperand atoi = new NasmLabel("atoi");

        //Assignation de code de l’opération à effectuer dans eax
        nasm.ajouteInst(new NasmMov(label,reg_eax, sinput,""));
        //Lecture et stockage d'une ligne à l’adresse pointée par eax
        nasm.ajouteInst(new NasmCall(null, realine,""));
        //Met dans eax l’entier obtenu de la chaîne pointée par eax
        nasm.ajouteInst(new NasmCall(null, atoi,""));
        //Assignation de la valeur de eax au résultat
        nasm.ajouteInst(new NasmMov(null, resultat, reg_eax,""));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstAffect inst) {
        //Récupération du label si existant
        NasmOperand label = getLabel(inst);

        //Récupération de l'opérande
        NasmOperand op1 = inst.op1.accept(this);
        //Récupération du résultat
        NasmOperand resultat = inst.result.accept(this);

        //Affectation de la valeur de l'opérande au résultat
        nasm.ajouteInst(new NasmMov(label,resultat,op1,""));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstParam inst) {
        //Récupération du label si existant
        NasmOperand label = getLabel(inst);

        //Récupération du paramètre
        NasmOperand op1 = inst.op1.accept(this);

        //Ajout du paramètre
        nasm.ajouteInst(new NasmPush(label,op1,""));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstReturn inst) {
        //Récupération du label si existant
        NasmOperand label = getLabel(inst);

        //Valeur de retour
        NasmOperand op1 = inst.op1.accept(this);

        //Création d'un nouveau registre
        NasmRegister reg_ebp = nasm.newRegister();
        //Assignation de la macro EBP au registre
        reg_ebp.colorRegister(Nasm.REG_EBP);

        //Valeur égale à 2 car 4*2=8, et ebp+8 pour traiter la valeur de retour
        // sachant que dans la classe NasmAddress l'index est multiplié par 4
        NasmConstant valeur_retour = new NasmConstant(2);

        //Adresse mémoire pointée
        NasmAddress adresse = new NasmAddress(reg_ebp,'+', valeur_retour);

        //Écriture de la valeur de retour
        nasm.ajouteInst(new NasmMov(label,adresse,op1,""));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstWrite inst) {
        //Récupération du label si existant
        NasmOperand label = getLabel(inst);

        //Création d'un nouveau registre
        NasmRegister reg_eax = nasm.newRegister();
        //Association de la macro EAX au registre
        reg_eax.colorRegister(Nasm.REG_EAX);

        //Récupération de la valeur à écrire
        NasmOperand op1 = inst.op1.accept(this);

        //Assignation de la valeur à écrire au registre eax
        nasm.ajouteInst(new NasmMov(label,reg_eax,op1,""));

        //Affichage de la valeur contenue dans eax
        nasm.ajouteInst(new NasmCall(null, new NasmLabel("iprintLF"),""));

        return null;
    }

    @Override
    public NasmOperand visit(C3aConstant oper) {
        //Retour de la constante
        return new NasmConstant(oper.val);
    }

    @Override
    public NasmOperand visit(C3aLabel oper) {
        //Retour du label
        return new NasmLabel(oper.toString());
    }

    @Override
    public NasmOperand visit(C3aTemp oper) {
        //Retour du registre temporaire
        return new NasmRegister(oper.num);
    }

    @Override
    public NasmOperand visit(C3aVar oper) {
        //Définition de l'adresse mémoire de la variable
        NasmAddress adresse;

        //La variable est contenue dans un tableau
        boolean estIndicee = (oper.index != null);
        //La variable est un paramètre
        boolean estParametre = oper.item.isParam;
        //La variable est contenue dans une fonction
        boolean estLocale = currentFct.getTable().getVar(oper.item.getIdentif()) != null;

        //Création d'un nouveau registre
        NasmRegister reg_ebp = nasm.newRegister();
        //Association de la macro EBP au registre
        reg_ebp.colorRegister(Nasm.REG_EBP);

        //Si la variable n'est pas située dans un tableau
        if (!estIndicee) {
            //Indice pour le calcul avec le registre ebp
            NasmConstant indice;
            //Si la variable est un paramètre
            if (estParametre) {
                //Calcul de l'index à additioner au registre ebp
                // sachant que dans la classe NasmAddress l'index est multiplié par 4
                indice = new NasmConstant((currentFct.getNbArgs() * 2) - oper.item.getAdresse());
                //Initialisation de l'adresse de la variable dans le registre ebp
                adresse = new NasmAddress(reg_ebp, '+', indice);
            //Si au contraire ce n'est pas un paramètre
            } else
                //Si c'est une variable locale
                if (estLocale) {
                    //Si l'adresse de la variable est supérieure à 0
                    if (oper.item.getAdresse() > 0)
                        //Initialisation de l'indice
                        indice = new NasmConstant(oper.item.getAdresse());
                    else
                        //Ajout de 1 à l'adresse pour soustraire ebp de 4 afin d'obtenir la première variable locale,
                        // sachant que l'indice est multiplié par 4 dans la classe NasmAddress
                        indice = new NasmConstant(oper.item.getAdresse()+1);
                    //Adresse de la variable calculée à partir de la base de la trame de pile (ebp)
                    adresse = new NasmAddress(reg_ebp, '-', indice);
                //Si c'est une variable globale
                } else
                    //L'adresse de la variable est son étiquette
                    adresse = new NasmAddress(new NasmLabel(oper.item.getIdentif()));
        //Si c'est une variable contenue dans un tableau
        } else {
            //Récupération du nom du tableau
            NasmLabel tableau = new NasmLabel(oper.item.getIdentif());
            //Récupération de l'indice où est située la variable
            NasmOperand indice = oper.index.accept(this);
            //Initialisation de l'adresse
            adresse = new NasmAddress(tableau, '+', indice);
        }

        //Retour de l'adresse
        return adresse;
    }

    @Override
    public NasmOperand visit(C3aFunction oper) {
        //Retour du label pointant la fonction
        return new NasmLabel(oper.val.identif);
    }
}