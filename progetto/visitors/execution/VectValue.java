package progetto2023_47.visitors.execution;

import static java.util.Objects.requireNonNull;

import static java.util.Objects.hash;

public class VectValue implements Value {

    private final int[] vector; // invariant: vector != null

	public VectValue(int[] vector) {
		this.vector = requireNonNull(vector);
	}

	public VectValue(int ind, int dim) {
		int[] vect;
		try{
			vect = new int[dim];
			vect[ind] = 1;	
		}catch(NegativeArraySizeException e){
			throw new InterpreterException(e);
		}catch(ArrayIndexOutOfBoundsException e){
			throw new InterpreterException(e);
		}catch(Throwable e){
			throw new InterpreterException(e);
		}
		this.vector = vect;
	}

	public int[] getVector(){
		return vector.clone();
	}

	public int getDim(){
		return vector.length;
	}

	public int getIndex(int i){
		int el;
		try {
			el = vector[i];
		} catch (ArrayIndexOutOfBoundsException e){
			throw new InterpreterException(e);
		}
		return el;
	}

	// scales every element of the vector by factor and returns the vector
	public int[] scaleFactor(int factor){
		for(int i = 0; i < vector.length; i++)
			vector[i] = vector[i]*factor; 
		return getVector();
	}

	@Override
	public VectValue toVect() {
		return this;
	}

	@Override
	public String toString() {
		String toString = new String("[");
        for (int i : vector)
            toString = toString + i + ";";
		// if at least one element has been added, the last ';' needs to be removed
        if(!toString.endsWith("["))
            toString = toString.substring(0, toString.length()-1);
        return toString += "]";
	}

	@Override
	public int hashCode() {
		return hash(vector);
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		/*
			vectors are equal if 
			1. they have the same length
			2. they have the same elements at the same indexes
		*/
		if (obj instanceof VectValue vt){
			if(this.getDim() != vt.getDim()) 
				return false;
			for(int i = 0; i < this.getDim(); i++){
				if(this.getIndex(i) != vt.getIndex(i))
					return false;
			}
			return true;
		}
		return false;
	}
}
