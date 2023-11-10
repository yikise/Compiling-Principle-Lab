package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.lexer.TokenKind;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.parser.table.Term;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import javax.print.attribute.standard.JobKOctets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {
    private final List<Instruction> irList  = new ArrayList<>();
    private final Stack<Object> symbolStack = new Stack<>();

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
        if (currentToken.getKind() == TokenKind.fromString("id")) {
            symbolStack.push(IRVariable.named(currentToken.getText()));
        } else if (currentToken.getKind() == TokenKind.fromString("IntConst")) {
            symbolStack.push(IRImmediate.of(Integer.parseInt(currentToken.getText())));
        } else {
            symbolStack.push(null);
        }
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
        switch (production.index()) {
            case 2, 3, 4, 5 -> {
                for (Term body : production.body()) {
                    symbolStack.pop();
                }
                symbolStack.push(null);
            }
            case 6 -> { // S -> id = E;
                Object E = symbolStack.pop(); // E
                symbolStack.pop(); // =
                Object id = symbolStack.pop(); // id
                Instruction instruction = Instruction.createMov((IRVariable) id, (IRValue) E);
                irList.add(instruction);
                symbolStack.push(null);
            }
            case 7 -> { // S -> return E;
                Object E = symbolStack.pop(); // E
                symbolStack.pop(); // return
                Instruction instruction = Instruction.createRet((IRValue) E);
                irList.add(instruction);
                symbolStack.push(null);
            }
            case 8 -> { // E -> E + A;
                Object A = symbolStack.pop();
                symbolStack.pop();
                Object E = symbolStack.pop();
                IRVariable result = IRVariable.temp();
                Instruction instruction = Instruction.createAdd(result, (IRValue) E, (IRValue) A);
                irList.add(instruction);
                symbolStack.push(result);
            }
            case 9 -> { // E -> E - A;
                Object A = symbolStack.pop();
                symbolStack.pop();
                Object E = symbolStack.pop();
                IRVariable result = IRVariable.temp();
                Instruction instruction = Instruction.createSub(result, (IRValue) E, (IRValue) A);
                irList.add(instruction);
                symbolStack.push(result);
            }
            case 11 -> { // A -> A * B;
                Object A = symbolStack.pop();
                symbolStack.pop();
                Object B = symbolStack.pop();
                IRVariable result = IRVariable.temp();
                Instruction instruction = Instruction.createMul(result, (IRValue) B, (IRValue) A);
                irList.add(instruction);
                symbolStack.push(result);
            }
            case 13 -> { // B -> ( E );
                symbolStack.pop();
                Object E = symbolStack.pop();
                symbolStack.pop();
                symbolStack.push(E);
            }
            default -> {
                // do nothing
            }
        }
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
        // do nothing
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
        // do nothing
    }

    public List<Instruction> getIR() {
        // TODO
        return irList;
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
}

