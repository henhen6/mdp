package top.mddata.console.service.message.glue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import top.mddata.base.exception.BizException;
import top.mddata.console.service.message.glue.impl.SpringGlueFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GlueFactory 单元测试
 * <p>
 * 覆盖：正向功能、边界条件、编译异常、RCE 安全拦截、绕过尝试、误报防护
 *
 * @author test
 */
class GlueFactoryTest {

    private GlueFactory glueFactory;

    @BeforeEach
    void setUp() {
        // 每个测试前重置为默认实例
        GlueFactory.refreshInstance(1);
        glueFactory = GlueFactory.getInstance();
    }

    // ==================== 正向功能测试 ====================

    @Nested
    @DisplayName("exeGroovyScript - 正向功能")
    class ExeGroovyScriptPositive {

        @Test
        @DisplayName("简单表达式返回字符串")
        void simpleStringReturn() {
            Object result = glueFactory.exeGroovyScript("'hello'", Map.of());
            assertEquals("hello", result);
        }

        @Test
        @DisplayName("简单表达式返回数值")
        void simpleNumericReturn() {
            Object result = glueFactory.exeGroovyScript("1 + 2", Map.of());
            assertEquals(3, result);
        }

        @Test
        @DisplayName("使用参数并返回 Map")
        void useParamsReturnMap() {
            String script = "def r = [:]; r.name = name; r.greeting = (\"Hi, ${name}\" as String); r";
            Map<String, Object> params = new HashMap<>();
            params.put("name", "Alice");

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) glueFactory.exeGroovyScript(script, params);
            assertEquals("Alice", result.get("name"));
            assertEquals("Hi, Alice", result.get("greeting"));
        }

        @Test
        @DisplayName("条件逻辑")
        void conditionalLogic() {
            String script = "if (x > 10) { 'big' } else { 'small' }";
            Map<String, Object> params = Map.of("x", 20);
            assertEquals("big", glueFactory.exeGroovyScript(script, params));

            params = Map.of("x", 5);
            assertEquals("small", glueFactory.exeGroovyScript(script, params));
        }

        @Test
        @DisplayName("集合操作 - List")
        void listOperations() {
            String script = "def list = [1, 2, 3]; list.sum()";
            assertEquals(6, glueFactory.exeGroovyScript(script, Map.of()));
        }

        @Test
        @DisplayName("集合操作 - Map 遍历")
        void mapOperations() {
            String script = "def m = [a:1, b:2]; m.collect{ k,v -> v }.sum()";
            assertEquals(3, glueFactory.exeGroovyScript(script, Map.of()));
        }

        @Test
        @DisplayName("字符串处理")
        void stringManipulation() {
            String script = "name.toUpperCase() + '-' + name.length()";
            Map<String, Object> params = Map.of("name", "hello");
            assertEquals("HELLO-5", glueFactory.exeGroovyScript(script, params));
        }

        @Test
        @DisplayName("闭包和高阶函数")
        void closureAndHigherOrderFunctions() {
            String script = "def nums = [1,2,3,4,5]; nums.findAll { it % 2 == 0 }.sum()";
            assertEquals(6, glueFactory.exeGroovyScript(script, Map.of()));
        }

        @Test
        @DisplayName("返回 null")
        void returnNull() {
            String script = "null";
            assertNull(glueFactory.exeGroovyScript(script, Map.of()));
        }

        @Test
        @DisplayName("返回空 Map")
        void returnEmptyMap() {
            String script = "[:]";
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) glueFactory.exeGroovyScript(script, Map.of());
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("GString 字符串插值")
        void gstringInterpolation() {
            String script = "(\"Hello, ${name}! You are ${age} years old.\" as String)";
            Map<String, Object> params = Map.of("name", "Bob", "age", 30);
            assertEquals("Hello, Bob! You are 30 years old.", glueFactory.exeGroovyScript(script, params));
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("exeGroovyScript - 边界条件")
    class ExeGroovyScriptBoundary {

        @Test
        @DisplayName("null 脚本抛 IllegalArgumentException")
        void nullScript() {
            assertThrows(IllegalArgumentException.class,
                    () -> glueFactory.exeGroovyScript(null, Map.of()));
        }

        @Test
        @DisplayName("空字符串抛 IllegalArgumentException")
        void emptyScript() {
            assertThrows(IllegalArgumentException.class,
                    () -> glueFactory.exeGroovyScript("", Map.of()));
        }

        @Test
        @DisplayName("纯空白脚本抛 IllegalArgumentException")
        void whitespaceOnlyScript() {
            assertThrows(IllegalArgumentException.class,
                    () -> glueFactory.exeGroovyScript("   \t\n  ", Map.of()));
        }

        @Test
        @DisplayName("params 为 null 时脚本仍可执行")
        void nullParams() {
            // Groovy Binding 接受 null map
            Object result = glueFactory.exeGroovyScript("42", null);
            assertEquals(42, result);
        }

        @Test
        @DisplayName("params 为空 Map")
        void emptyParams() {
            Object result = glueFactory.exeGroovyScript("'ok'", new HashMap<>());
            assertEquals("ok", result);
        }
    }

    // ==================== 编译异常测试 ====================

    @Nested
    @DisplayName("exeGroovyScript - 编译异常")
    class ExeGroovyScriptCompileError {

        @Test
        @DisplayName("语法错误的脚本抛 BizException")
        void syntaxError() {
            assertThrows(BizException.class,
                    () -> glueFactory.exeGroovyScript("def x = { unclosed", Map.of()));
        }

        @Test
        @DisplayName("未闭合的字符串抛 BizException")
        void unclosedString() {
            assertThrows(BizException.class,
                    () -> glueFactory.exeGroovyScript("def x = 'unclosed", Map.of()));
        }
    }

    // ==================== RCE 安全拦截测试 ====================

    @Nested
    @DisplayName("安全拦截 - RCE payload 必须被阻止")
    class SecurityRceBlocking {

        @Test
        @DisplayName("漏洞报告原始 payload（.execute + new File）被拦截")
        void originalRcePayload() {
            String payload = "def p='/tmp/LAMP_SELFTEST_001';"
                             + "new File(p+'.uid').text='id'.execute().text+'LAMP_GLUE_RCE_MARKER';"
                             + "new File(p).text='1';[:]";
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(payload, Map.of()));
        }

        @Test
        @DisplayName("'.execute()' 被拦截")
        void dotExecute() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript("'id'.execute().text", Map.of()));
        }

        @Test
        @DisplayName("'.exec()' 被拦截")
        void dotExec() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript("'ls'.exec().text", Map.of()));
        }

        @Test
        @DisplayName("'new File(...)' 被拦截")
        void newFile() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript("new File('/tmp/x').text = 'pwned'; [:]", Map.of()));
        }

        @Test
        @DisplayName("Runtime.getRuntime() 被拦截")
        void runtimeGetRuntime() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(
                            "Runtime.getRuntime().exec('id'); [:]", Map.of()));
        }

        @Test
        @DisplayName("ProcessBuilder 被拦截")
        void processBuilder() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(
                            "new ProcessBuilder(['id']).start(); [:]", Map.of()));
        }

        @Test
        @DisplayName("Class.forName 被拦截")
        void classForName() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(
                            "Class.forName('java.lang.Runtime'); [:]", Map.of()));
        }

        @Test
        @DisplayName("反射 - getMethod 被拦截")
        void reflectionGetMethod() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(
                            "def m = ''.class.getMethod('toString'); [:]", Map.of()));
        }

        @Test
        @DisplayName("反射 - getDeclaredConstructor 被拦截")
        void reflectionGetDeclaredConstructor() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(
                            "def c = String.class.getDeclaredConstructor(); [:]", Map.of()));
        }

        @Test
        @DisplayName("System.exit 被拦截")
        void systemExit() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript("System.exit(0); [:]", Map.of()));
        }

        @Test
        @DisplayName("System.getenv 被拦截")
        void systemGetenv() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript("System.getenv(); [:]", Map.of()));
        }

        @Test
        @DisplayName("System.setProperty 被拦截")
        void systemSetProperty() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(
                            "System.setProperty('a','b'); [:]", Map.of()));
        }

        @Test
        @DisplayName("GroovyShell 被拦截")
        void groovyShell() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(
                            "new GroovyShell().evaluate('1+1'); [:]", Map.of()));
        }

        @Test
        @DisplayName("GroovyClassLoader 被拦截")
        void groovyClassLoader() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(
                            "new GroovyClassLoader().parseClass('1'); [:]", Map.of()));
        }

        @Test
        @DisplayName("metaClass 被拦截")
        void metaClass() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(
                            "String.metaClass.hack = { -> 'pwned' }; [:]", Map.of()));
        }

        @Test
        @DisplayName("ExpandoMetaClass 被拦截")
        void expandoMetaClass() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(
                            "def emc = new ExpandoMetaClass(String); [:]", Map.of()));
        }

        @Test
        @DisplayName("@ASTTest 注解被拦截")
        void astTestAnnotation() {
            // 使用短名 @ASTTest（不带完整包名）才能被 Token 扫描命中
            String payload = "import groovy.transform.ASTTest\n"
                             + "@ASTTest(value = { assert true })\n"
                             + "class Foo {}\n[:]";
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(payload, Map.of()));
        }

        @Test
        @DisplayName("@Grab 注解被拦截")
        void grabAnnotation() {
            String payload = "@Grab('commons-lang:commons-lang:2.6')\n"
                             + "import org.apache.commons.lang.StringUtils\n[:]";
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(payload, Map.of()));
        }

        @Test
        @DisplayName("Socket 被拦截")
        void socketUsage() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(
                            "new Socket('localhost', 8080); [:]", Map.of()));
        }

        @Test
        @DisplayName("ObjectInputStream/readObject 被拦截")
        void objectInputStream() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(
                            "def ois = new ObjectInputStream(null); ois.readObject(); [:]", Map.of()));
        }

        @Test
        @DisplayName("FileWriter 被拦截")
        void fileWriter() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(
                            "new FileWriter('/tmp/x').write('a'); [:]", Map.of()));
        }

        @Test
        @DisplayName("FileReader 被拦截")
        void fileReader() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(
                            "new FileReader('/etc/passwd').text; [:]", Map.of()));
        }
    }

    // ==================== 安全绕过尝试测试 ====================

    @Nested
    @DisplayName("安全拦截 - 绕过尝试必须被拦截")
    class SecurityBypassAttempts {

        @Test
        @DisplayName("字符串插值中嵌入 .execute 被正则层拦截")
        void stringInterpolationExecute() {
            // 即使 Token 层剥离了字符串，正则层仍能捕获 ${...execute...}
            String payload = "def cmd = \"ls\"; \"${cmd.execute().text}\"; [:]";
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(payload, Map.of()));
        }

        @Test
        @DisplayName("字符串插值中嵌入 Runtime 被正则层拦截")
        void stringInterpolationRuntime() {
            String payload = "def r = \"${Runtime.getRuntime()}\"; [:]";
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(payload, Map.of()));
        }

        @Test
        @DisplayName(".forName() 被正则层拦截")
        void forNamePattern() {
            String payload = "def clazz = Class.forName('java.lang.String'); [:]";
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(payload, Map.of()));
        }

        @Test
        @DisplayName(".execute() 带空格也被拦截")
        void executeWithSpaces() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript("'id'.execute ().text", Map.of()));
        }

        @Test
        @DisplayName(".exec() 带空格也被拦截")
        void execWithSpaces() {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript("'ls'.exec ().text", Map.of()));
        }
    }

    // ==================== 误报防护测试 ====================

    @Nested
    @DisplayName("安全校验 - 不应误报合法脚本")
    class SecurityFalsePositivePrevention {

        @Test
        @DisplayName("字符串字面量中包含 'File' 不应误报")
        void fileTokenInStringLiteral() {
            String script = "def path = 'File'; path.length()";
            assertDoesNotThrow(() -> glueFactory.exeGroovyScript(script, Map.of()));
        }

        @Test
        @DisplayName("字符串字面量中包含 'Runtime' 不应误报")
        void runtimeTokenInStringLiteral() {
            String script = "def msg = 'Runtime error occurred'; msg";
            assertDoesNotThrow(() -> glueFactory.exeGroovyScript(script, Map.of()));
        }

        @Test
        @DisplayName("注释中包含危险 Token 不应误报")
        void dangerousTokenInComment() {
            String script = "// This script does not use Runtime or File\n[:]";
            assertDoesNotThrow(() -> glueFactory.exeGroovyScript(script, Map.of()));
        }

        @Test
        @DisplayName("块注释中包含危险 Token 不应误报")
        void dangerousTokenInBlockComment() {
            String script = "/* Runtime Process File */\ndef x = 1 + 2; x";
            assertDoesNotThrow(() -> glueFactory.exeGroovyScript(script, Map.of()));
        }

        @Test
        @DisplayName("双引号字符串中包含 'Process' 不应误报")
        void processInDoubleQuotedString() {
            String script = "def msg = \"Process completed successfully\"; msg";
            assertDoesNotThrow(() -> glueFactory.exeGroovyScript(script, Map.of()));
        }

        @Test
        @DisplayName("三引号字符串中包含危险 Token 不应误报")
        void dangerousTokenInTripleQuotedString() {
            String script = "def doc = '''Usage: Runtime, Process, File'''\ndoc.length()";
            assertDoesNotThrow(() -> glueFactory.exeGroovyScript(script, Map.of()));
        }

        @Test
        @DisplayName("变量名 profile 含 'File' 子串但不应误报")
        void profileVariableName() {
            // "profile" 不包含独立的 "File" token（F 大写），但包含 "file" 子串
            // Token 黑名单是精确匹配的 "File"（F大写），所以 profile 不会命中
            String script = "def userProfile = 'admin'; userProfile";
            assertDoesNotThrow(() -> glueFactory.exeGroovyScript(script, Map.of()));
        }

        @Test
        @DisplayName("含 'Socket' 在字符串中的合法脚本")
        void socketInString() {
            String script = "def msg = 'WebSocket connection established'; msg";
            assertDoesNotThrow(() -> glueFactory.exeGroovyScript(script, Map.of()));
        }
    }

    // ==================== 参数化安全测试 ====================

    @Nested
    @DisplayName("参数化安全拦截测试")
    class ParameterizedSecurityTests {

        static Stream<Arguments> dangerousPayloads() {
            return Stream.of(
                    Arguments.of("命令执行 - execute", "'whoami'.execute().text"),
                    Arguments.of("命令执行 - exec", "'id'.exec().text"),
                    Arguments.of("文件写入", "new File('/tmp/x').text='a'"),
                    Arguments.of("文件读取", "new File('/etc/passwd').text"),
                    Arguments.of("Runtime", "Runtime.getRuntime()"),
                    Arguments.of("ProcessBuilder", "new ProcessBuilder(['ls'])"),
                    Arguments.of("Class.forName", "Class.forName('java.lang.Runtime')"),
                    Arguments.of("System.exit", "System.exit(1)"),
                    Arguments.of("System.getenv", "System.getenv('PATH')"),
                    Arguments.of("反射 getMethod", "''.class.getMethod('toString')"),
                    Arguments.of("反射 newInstance", "String.class.newInstance()"),
                    Arguments.of("GroovyShell", "new GroovyShell().evaluate('1')"),
                    Arguments.of("metaClass", "String.metaClass.x = {->1}"),
                    Arguments.of("Socket", "new Socket('localhost',80)"),
                    Arguments.of("URLClassLoader", "new URLClassLoader(null)"),
                    Arguments.of("readObject", "null.readObject()"),
                    Arguments.of("defineClass", "null.defineClass()"),
                    Arguments.of("Expando", "new Expando()")
            );
        }

        static Stream<Arguments> safeScripts() {
            return Stream.of(
                    Arguments.of("简单 Map", "[:]"),
                    Arguments.of("字符串拼接", "'hello' + ' ' + 'world'"),
                    Arguments.of("数值运算", "100 * 2 + 50"),
                    Arguments.of("List 操作", "[1,2,3].collect { it * 2 }"),
                    Arguments.of("Map 操作", "[a:1,b:2].keySet().toList()"),
                    Arguments.of("闭包", "def fn = { x -> x + 1 }; fn(10)"),
                    Arguments.of("条件", "true ? 'yes' : 'no'"),
                    Arguments.of("switch", "def x=1; switch(x){case 1:'one';break;default:'other'}"),
                    Arguments.of("正则匹配", "'hello123' =~ /\\d+/"),
                    Arguments.of("GString", "\"value=${1+2}\""),
                    Arguments.of("字符串包含 File 字面量", "def s = 'File'; s"),
                    Arguments.of("字符串包含 Runtime 字面量", "def s = 'Runtime'; s")
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("dangerousPayloads")
        @DisplayName("危险 payload 必须全部被拦截")
        void allDangerousPayloadsBlocked(String description, String payload) {
            assertThrows(SecurityException.class,
                    () -> glueFactory.exeGroovyScript(payload, Map.of()),
                    "Payload 应被拦截: " + description);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("safeScripts")
        @DisplayName("合法脚本必须正常执行")
        void allSafeScriptsAllowed(String description, String script) {
            assertDoesNotThrow(
                    () -> glueFactory.exeGroovyScript(script, Map.of()),
                    "合法脚本不应被拦截: " + description);
        }
    }

    // ==================== 缓存机制测试 ====================

    @Nested
    @DisplayName("脚本缓存机制")
    class ScriptCaching {

        @Test
        @DisplayName("相同脚本执行两次结果一致（验证缓存不破坏功能）")
        void sameScriptTwice() {
            String script = "def r = [sum: a + b]; r";
            Map<String, Object> params = Map.of("a", 10, "b", 20);

            @SuppressWarnings("unchecked")
            Map<String, Object> result1 = (Map<String, Object>) glueFactory.exeGroovyScript(script, params);
            @SuppressWarnings("unchecked")
            Map<String, Object> result2 = (Map<String, Object>) glueFactory.exeGroovyScript(script, params);

            assertEquals(result1, result2);
            assertEquals(30, result1.get("sum"));
        }
    }

    // ==================== 实例管理测试 ====================

    @Nested
    @DisplayName("GlueFactory 实例管理")
    class InstanceManagement {

        @Test
        @DisplayName("getInstance 返回非空实例")
        void getInstanceNotNull() {
            assertNotNull(GlueFactory.getInstance());
        }

        @Test
        @DisplayName("refreshInstance(0) 创建基础 GlueFactory")
        void refreshToBasic() {
            GlueFactory.refreshInstance(0);
            GlueFactory instance = GlueFactory.getInstance();
            assertNotNull(instance);
            assertFalse(instance instanceof SpringGlueFactory);
        }

        @Test
        @DisplayName("refreshInstance(1) 创建 SpringGlueFactory")
        void refreshToSpring() {
            GlueFactory.refreshInstance(1);
            GlueFactory instance = GlueFactory.getInstance();
            assertInstanceOf(SpringGlueFactory.class, instance);
        }

        @Test
        @DisplayName("refreshInstance 后新实例可正常执行脚本")
        void refreshThenExecute() {
            GlueFactory.refreshInstance(0);
            Object result = GlueFactory.getInstance().exeGroovyScript("1 + 1", Map.of());
            assertEquals(2, result);
        }
    }

    // ==================== loadNewInstance 测试 ====================

    @Nested
    @DisplayName("loadNewInstance")
    class LoadNewInstanceTests {

        @Test
        @DisplayName("null 脚本抛 BizException")
        void nullScript() {
            assertThrows(BizException.class,
                    () -> glueFactory.loadNewInstance(null));
        }

        @Test
        @DisplayName("空字符串抛 BizException")
        void emptyScript() {
            assertThrows(BizException.class,
                    () -> glueFactory.loadNewInstance(""));
        }

        @Test
        @DisplayName("空白字符串抛 BizException")
        void whitespaceScript() {
            assertThrows(BizException.class,
                    () -> glueFactory.loadNewInstance("   "));
        }

        @Test
        @DisplayName("不包含 MsgTaskStrategy 实现的脚本抛 IllegalArgumentException")
        void nonStrategyScript() {
            // 一个合法但不实现 MsgTaskStrategy 的 Groovy 类
            String script = "class Foo { def bar() { 'hello' } }";
            assertThrows(IllegalArgumentException.class,
                    () -> glueFactory.loadNewInstance(script));
        }

        @Test
        @DisplayName("包含危险 Token 的脚本被安全校验拦截")
        void dangerousScriptBlocked() {
            String script = "class Evil { def exec() { 'id'.execute().text } }";
            assertThrows(SecurityException.class,
                    () -> glueFactory.loadNewInstance(script));
        }

        @Test
        @DisplayName("语法错误的脚本抛 BizException")
        void syntaxErrorScript() {
            assertThrows(BizException.class,
                    () -> glueFactory.loadNewInstance("class { invalid syntax"));
        }
    }
}
