import c3a.*;
import nasm.*;
import ts.Ts;
import ts.TsItemFct;

public class C3a2nasm implements C3aVisitor<NasmOperand> {
    private C3a c3a;
    private Nasm nasm;
    private Ts tableGlobale;
    private TsItemFct currentFct;

    //Addition
    @Override
    public NasmOperand visit(C3aInstAdd inst) {
        //Récupération du label si existant
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null;
        //Récupération de la première opérande
        NasmOperand op1 = inst.op1.accept(this);
        //Récupération de la deuxième opérande
        NasmOperand op2 = inst.op2.accept(this);
        //Récupération du résultat
        NasmOperand result = inst.result.accept(this);
        //Assignation de la première opérande dans le résultat
        nasm.ajouteInst(new NasmMov(label, result, op1, ""));
        //Addition de la deuxième opérande au résultat
        nasm.ajouteInst(new NasmAdd(null, result, op2, ""));
        return null;
    }

    //Soustraction
    @Override
    public NasmOperand visit(C3aInstSub inst) {
        //Récupération du label si existant
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null;
        //Récupération de la première opérande
        NasmOperand op1 = inst.op1.accept(this);
        //Récupération de la deuxième opérande
        NasmOperand op2 = inst.op2.accept(this);
        //Récupération du résultat
        NasmOperand result = inst.result.accept(this);
        //Assignation de la première opérande dans le résultat
        nasm.ajouteInst(new NasmMov(label, result, op1, ""));
        //Soustraction de la deuxième opérande au résultat
        nasm.ajouteInst(new NasmSub(null, result, op2, ""));
        return null;
    }

    //Multiplication
    @Override
    public NasmOperand visit(C3aInstMult inst) {
        //Récupération du label si existant
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null;
        //Récupération de la première opérande
        NasmOperand op1 = inst.op1.accept(this);
        //Récupération de la deuxième opérande
        NasmOperand op2 = inst.op2.accept(this);
        //Récupération du résultat
        NasmOperand result = inst.result.accept(this);
        //Assignation de la première opérande dans le résultat
        nasm.ajouteInst(new NasmMov(label, result, op1, ""));
        //Multiplication de la deuxième opérande au résultat
        nasm.ajouteInst(new NasmMul(null, result, op2, ""));
        return null;
    }

    //Division
    @Override
    public NasmOperand visit(C3aInstDiv inst) {
        //Récupération du label si existant
        NasmOperand label = (inst.label != null) ? inst.label.accept(this) : null;
        //Récupération de la première opérande
        NasmOperand op1 = inst.op1.accept(this);
        //Récupération de la deuxième opérande
        NasmOperand op2 = inst.op2.accept(this);
        //Récupération du résultat
        NasmOperand result = inst.result.accept(this);
        //Assignation de la première opérande dans le résultat
        nasm.ajouteInst(new NasmMov(label, result, op1, ""));
        //Division de la deuxième opérande au résultat
        nasm.ajouteInst(new NasmDiv(null, result, op2, ""));
        return null;
    }

    @Override
    public NasmOperand visit(C3aInstCall inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aInstFBegin inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aInst inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aInstJumpIfLess inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aInstRead inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aInstAffect inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aInstFEnd inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aInstJumpIfEqual inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aInstJumpIfNotEqual inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aInstJump inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aInstParam inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aInstReturn inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aInstWrite inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aConstant oper) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aLabel oper) {
        return null;
    }

    //Temporaire
    @Override
    public NasmOperand visit(C3aTemp oper) {
        return new NasmRegister(oper.num);
    }

    @Override
    public NasmOperand visit(C3aVar oper) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aFunction oper) {
        return null;
    }
}