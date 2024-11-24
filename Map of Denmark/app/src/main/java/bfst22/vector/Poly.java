package bfst22.vector;

import java.util.Arrays;
import java.util.List;

public class Poly {
	public static void stitch(List<Drawable> parts){
		for(int i = 0; i < parts.size(); i++) {
			for (int j = i+1; j < parts.size(); j++) {
				PolyGon a = (PolyGon) parts.get(i), b = (PolyGon) parts.get(j);
				if (a.ids[a.ids.length-1] == b.ids[b.ids.length-1]) Poly.mirror(b);
				if (a.ids[a.ids.length-1] == b.ids[0]) {
					parts.set(i,Poly.combinePolyGons(a,b));
					parts.remove(b);
					--j;
				}
			}
		}
	}

	public static float[] center(PolyLine a){
		double midtx = 0, midty = 0;
		for(int i = 0; i < a.coords.length; i+=2){
			midtx += a.coords[i];
			midty += a.coords[i+1];
		}
		return new float[]{(float) midtx/(a.coords.length/3),(float) midty/(a.coords.length/3)};
	}

	public static void winding(PolyGon a){
		if(a.clockwise && !Poly.isCW(a) || !a.clockwise && Poly.isCW(a)) Poly.mirror(a);
	}

	// https://stackoverflow.com/questions/1165647/how-to-determine-if-a-list-of-polygon-points-are-in-clockwise-order
	public static boolean isCW(PolyLine a){
		double sum = 0.0;
		for(int i = 0; i < a.coords.length; i+=2) {
			float x2 = a.coords[(i + 2) % a.coords.length];
			float x1 = a.coords[i];
			float y2 = a.coords[(i + 3) % a.coords.length];
			float y1 = a.coords[i + 1];
			sum += (x2 - x1) * (y2 + y1);
		}
		return sum > 0.0;
	}

	public static float[] combineArrays(float[] a, float[] b){
		a = Arrays.copyOf(a, a.length + b.length);
		System.arraycopy(b, 0, a, a.length-b.length, b.length);
		return a;
	}

	public static long[] combineArrays(long[] a, long[] b){
		a = Arrays.copyOf(a, a.length + b.length);
		System.arraycopy(b, 0, a, a.length-b.length, b.length);
		return a;
	}

	public static PolyGon combinePolyGons(PolyGon a, PolyGon b){
		float[] coords = Poly.combineArrays(a.coords,b.coords);
		long[] ids = Poly.combineArrays(a.ids,b.ids);
		return new PolyGon(new PolyLine(ids,coords),a.clockwise);
	}

	public static float[] mirrorArray(float[] a, int u){
		float[] b = new float[a.length];
		for(int i = 0; i < a.length; i+=u)
			for(int j = 0; j < u; j++)
				b[a.length-i-j-1] = a[i+u-j-1];
		return b;
	}

	public static long[] mirrorArray(long[] a, int u){
		long[] b = new long[a.length];
		for(int i = 0; i < a.length; i+=u)
			for(int j = 0; j < u; j++)
				b[a.length-i-j-1] = a[i+u-j-1];
		return b;
	}

	public static void mirror(PolyLine a){
		a.coords = Poly.mirrorArray(a.coords,2);
		a.ids = Poly.mirrorArray(a.ids,1);
	}
}
