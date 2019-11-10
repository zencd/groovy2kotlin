class AttributeExpr {
    private String norm = "123"
    String getSome() {
        return this.@norm
    }
}
