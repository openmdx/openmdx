package test.manifold.preprocessor;

import #if JAVA_8 javax.transaction.UserTransaction #else jakarta.transaction.UserTransaction #endif ;
import #if JMI_CLASSIC java.util.Date #else java.time.Instant #endif ; 

public class TestManifoldPreprocessor {

	public static void main(String[] args) {
	    Class<?> transactionClass = UserTransaction.class;
	    System.out.println("UserTransaction class=" + transactionClass.getName());
	    Object dateTime = #if JMI_CLASSIC new Date() #else Instant.now() #endif; 
	    System.out.println("org::w3c::dateTime class=" + dateTime.getClass().getName() + ", now=" + dateTime);
	}

}
