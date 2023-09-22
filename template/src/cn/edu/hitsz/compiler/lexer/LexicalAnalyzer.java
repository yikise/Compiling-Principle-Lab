package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.symtab.SymbolTableEntry;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;
    private final List<Token> TokenList;
    private String buffer = "";


    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.TokenList = new ArrayList<>();
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) throws IOException {
        // TODO: 词法分析前的缓冲区实现
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        buffer = new String(Files.readAllBytes(Paths.get(path)));
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        // TODO: 自动机实现的词法分析过程
        int i = 0;
        while (i < buffer.length()) {
            char c = buffer.charAt(i);
            //如果是空格，则跳过，i++
            if (Character.isWhitespace(c)) {
                i++;
            } else if (Character.isLetter(c)) {
                //如果是字母，则令start=i，开始循环，找到这个词
                int start = i;
                while (i < buffer.length() && Character.isLetterOrDigit(buffer.charAt(i))) {
                    i++;
                }
                //得到词语
                String text = buffer.substring(start, i);
                //判断是否是如int、return的关键词，若是，直接添加
                if (TokenKind.isAllowed(text)) {
                    TokenKind tokenKind = TokenKind.fromString(text);
                    TokenList.add(Token.simple(tokenKind));
                } else {
                    //若不是，则代表是字符串Id，先存入符号表中
                    if (!symbolTable.has(text)) {
                        symbolTable.add(text);
                    }
                    //添加id进TokenList
                    TokenList.add(Token.normal("id", text));
                }
            } else if(Character.isDigit(c)) {
                //若c是数字
                int start = i;
                while (i < buffer.length() && Character.isDigit(buffer.charAt(i))) {
                    i++;
                }
                //得到词语
                String text = buffer.substring(start, i);
                //将数字加入进TokenList
                TokenList.add(Token.normal("IntConst", text));
            } else if (c == ';') {
                //当c是终结符号时
                String text = buffer.substring(i, i+1);
                //将数字加入进TokenList
                TokenList.add(Token.normal("Semicolon", text));
            } else if (c == '*' || c == '=') {
                //当c是*或=时，需要判断后面是否还有一个c
                int j = i + 1;
                if (j >= buffer.length()) {
                    String text = buffer.substring(i, i + 1);
                    if (TokenKind.isAllowed(text)) {
                        TokenKind tokenKind = TokenKind.fromString(text);
                        TokenList.add(Token.simple(tokenKind));
                    }
                } else if (j == c) {
                    String text = buffer.substring(i, j + 1);
                    if (TokenKind.isAllowed(text)) {
                        TokenKind tokenKind = TokenKind.fromString(text);
                        TokenList.add(Token.simple(tokenKind));
                    }
                }
            } else {
                //其余符号都只有一个，直接判断。
                String text = buffer.substring(i, i + 1);
                if (TokenKind.isAllowed(text)) {
                    TokenKind tokenKind = TokenKind.fromString(text);
                    TokenList.add(Token.simple(tokenKind));
                }
            }

        }
    }

    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
        return TokenList;
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }


}
