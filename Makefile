JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
		Main.java \
		Process.java \
		ProcessInterface.java \
		ProcessMain.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
		$(RM) ProcessMain.class
		$(RM) Process.class
		$(RM) ProcessInterface.class
		$(RM) ProcessMain.class