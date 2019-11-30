package gtk.ust

class AAnnotated extends ANode {
    List<AAnnotation> annotations = new ArrayList<AAnnotation>()
    AClass declaringClass
    boolean synthetic
    boolean hasNoRealSourcePositionFlag
}
