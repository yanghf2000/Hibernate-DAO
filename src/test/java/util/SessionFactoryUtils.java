package util;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.github.yanghf2000.namingStrategy.SimplePhysicalNamingStrategy;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class SessionFactoryUtils {
	
	private static SessionFactory sf;

	private SessionFactoryUtils() {}
	
	public static synchronized SessionFactory build() {
		if(sf != null) {
			return sf;
		}
		
		StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
		        .configure( "resources/hibernate.cfg.xml" )
		        .build();

		MetadataSources metadataSources = new MetadataSources( standardRegistry );
		try {
			for(Class c : getEntityClasses()) {
				metadataSources.addAnnotatedClass(c);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Metadata metadata = metadataSources
		    .getMetadataBuilder()
		    .applyPhysicalNamingStrategy(new SimplePhysicalNamingStrategy())
		    .build();

		sf = metadata.getSessionFactoryBuilder().build();
		return sf;
	}
	
	@SuppressWarnings("rawtypes")
	private static List<Class> getEntityClasses() throws MalformedURLException, ClassNotFoundException{
		List<Class> list = new ArrayList<>();
		
		URL url = SessionFactoryUtils.class.getClassLoader().getResource("entity");
 		File file = new File(url.getPath());
		File[] fs = file.listFiles();
		for(File f : fs) {
			Class clazz = Class.forName("entity." + f.getName().substring(0, f.getName().indexOf(".class")));
			if((clazz.getModifiers() & Modifier.ABSTRACT) == 0) {
				list.add(clazz);
			}
		}
		System.out.println(list.size());
		System.out.println(list);
		return list;
	}
	
}
