package gtk

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.TupleExpression

/**
 * Works like {@link org.codehaus.groovy.ast.ClassNode#tryFindPossibleMethod)}.
 * But the original method is unable to find a method if any of the given args is a null constant.
 * This implementation does match against nulls too
 *
 * TODO check the case of invocation like `method(null as SomeType)`
 */
class MethodMatcher {

    static MethodNode findMethod(ClassNode self, String name, Expression arguments) {
        if (!(arguments instanceof TupleExpression)) {
            return null
        }
        TupleExpression args = (TupleExpression) arguments;

        def findStrictly = { tryFindPossibleMethod(self, name, args, false) }
        def findLoosely = { tryFindPossibleMethod(self, name, args, true) }
        return findStrictly() ?: findLoosely()
    }

    private static MethodNode tryFindPossibleMethod(ClassNode self, String name, TupleExpression args, boolean loose) {
        // TODO this won't strictly be true when using list expansion in argument calls
        int count = args.getExpressions().size();

        MethodNode res = null;
        ClassNode node = self;
        while (true) {
            for (MethodNode method : node.getMethods(name)) {
                if (hasCompatibleNumberOfArgs(method, count)) {
                    boolean match = true;
                    for (int i = 0; i != count; ++i)
                        if (!hasCompatibleType(args, method, i, loose)) {
                            match = false;
                            break;
                        }

                    if (match) {
                        if (res == null)
                            res = method;
                        else {
                            if (res.getParameters().length != count)
                                return null;
                            if (node.equals(self))
                                return null;

                            match = true;
                            for (int i = 0; i != count; ++i)
                                // prefer super method if it matches better
                                if (!hasExactMatchingCompatibleType(res, method, i)) {
                                    match = false;
                                    break;
                                }
                            if (!match)
                                return null;
                        }
                    }
                }
            }
            node = node.getSuperClass();
            if (node == null) {
                break
            }
        }

        return res;
    }

    private static boolean hasCompatibleNumberOfArgs(MethodNode method, int count) {
        int lastParamIndex = method.getParameters().length - 1;
        return method.getParameters().length == count || (isPotentialVarArg(method, lastParamIndex) && count >= lastParamIndex);
    }

    private static boolean hasCompatibleType(TupleExpression args, MethodNode method, int i, boolean loose) {
        int lastParamIndex = method.getParameters().length - 1;
        def arg = args.getExpression(i)
        return (loose && GtkUtils.isNullConstant(arg)) \
            || (i <= lastParamIndex && arg.getType().isDerivedFrom(method.getParameters()[i].getType())) \
            || (isPotentialVarArg(method, lastParamIndex) && i >= lastParamIndex && arg.getType().isDerivedFrom(method.getParameters()[lastParamIndex].getType().componentType));
    }

    private static boolean isPotentialVarArg(MethodNode newCandidate, int lastParamIndex) {
        return lastParamIndex >= 0 && newCandidate.getParameters()[lastParamIndex].getType().isArray();
    }

    private static boolean hasExactMatchingCompatibleType(MethodNode current, MethodNode newCandidate, int i) {
        int lastParamIndex = newCandidate.getParameters().length - 1;
        return current.getParameters()[i].getType().equals(newCandidate.getParameters()[i].getType()) \
                || (isPotentialVarArg(newCandidate, lastParamIndex) && i >= lastParamIndex && current.getParameters()[i].getType().equals(newCandidate.getParameters()[lastParamIndex].getType().componentType));
    }
}
