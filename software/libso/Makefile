all: objs
	g++ -shared -o libmathoperators.so foo.o bar.o
	g++ -ldl -I ./ main.cpp

objs: foo.cpp bar.cpp
	g++ -c foo.cpp foo.o
	g++ -c bar.cpp bar.o

clean:
	rm *.o
	rm *.so
	rm *.out

