public class TestClass {
    
    public void main(String[] args) {
        if(args != null) {
            for(String arg : args) {
                System.out.println("Your argument is " + arg);
            }
        }
    }
}