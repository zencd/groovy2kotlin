package gtk.ust

class AGenericsType extends ANode {
    String name
    boolean placeholder
    AClass lowerBound
    AClass type
    boolean resolved
    boolean wildcard
    List<AClass> upperBounds = new ArrayList<AClass>()
}
