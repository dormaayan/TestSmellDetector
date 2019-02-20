package extractTestFiles;
import java.io.File;
import java.io.IOException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;

public class FindClasses {

	public static void listClasses(File projectDir) {
		new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
			System.out.println(path);
			System.out.println(Strings.repeat("=", path.length()));
			try {
				new VoidVisitorAdapter<Object>() {
					@Override
					public void visit(ClassOrInterfaceDeclaration n, Object arg) {
						super.visit(n, arg);
						System.out.println(" * " + n.getName());
					}
				}.visit(JavaParser.parse(file), null);
				System.out.println(); // empty line
			} catch (IOException e) {
				new RuntimeException(e);
			}
		}).explore(projectDir);
	}

	public static void main(String[] args) {
		File projectDir = new File("/Users/Dor/Desktop/Coverage/cors-filter-cors-filter-1.0.1");
		listClasses(projectDir);
	}
}