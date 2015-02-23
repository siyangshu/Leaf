package leaf;

import processing.core.PApplet;
import javafx.geometry.*;
import javafx.scene.transform.*;
import java.util.*;
import java.lang.Math;



public class Canvas extends PApplet {
	
	final static double PI = 3.1415926535898;
	Leaf leaf;
	final int width = 1280;
	final int height = 800;
	
	public void setup() {
		size(width, height);
		background(200,220,255);
		leaf = new Leaf();
		noFill();
		stroke(56,135,0);
	}

	public void draw() {
		background(200,220,255);
//		leaf.draw(new Point2D(20, 600), new Point2D(1200, 10));
	}

	public void point(Point2D p1){
		point((float)p1.getX(), (float)p1.getY());
	}

	public void line(Point2D p1, Point2D p2){
		line((float)p1.getX(), (float)p1.getY(), (float)p2.getX(), (float)p2.getY());
	}
	
	public void bezier(Point2D p1, Point2D p2, Point2D p3, Point2D p4){
		bezier((float)p1.getX(), (float)p1.getY(), 
				(float)p2.getX(), (float)p2.getY(),
				(float)p3.getX(), (float)p3.getY(),
				(float)p4.getX(), (float)p4.getY()); 
	}


	class Leaf{
		final int nodesNum = 20;       // How many child leaves does a leaf has?
		final double leafAngle = 30;   // The angle between child leaf and parent leaf
		double leafSizeMin = 20.0;     // If a leaf's size is less than this, we simply draw a line. Can't too small or every pixel will be leaves. 

		void draw(Point2D start, Point2D end){
			if(start.distance(end) < leafSizeMin){
					line(start, end);
				return;
			}
			ArrayList<Point2D> nodes = buildNodes(start, end);
			drawLeafVein(nodes);
			for(int i = 1;i < nodes.size() - 1; i++){
				Point2D leafEnd;
				leafEnd = calcLeafEnd(nodes, i);
				draw(nodes.get(i), leafEnd);
			}

		}
		
		Point2D calcLeafEnd(ArrayList<Point2D> n, int i){
			// ArrayList<Point2D> n is an array of vein nodes. 
			// Given the new child leaf's root n.get(i), and 
			// the rotation angle, what is its end point?
			double degree;
			if(i % 2 == 0){
				degree = leafAngle;
			}else{
				degree = -leafAngle;
			}
			Point2D start = n.get(0);
			Point2D end = n.get(n.size() - 1);
			Point2D point = n.get(i);
			Point2D vec = end.subtract(start);
			vec = new Rotate(degree).transform(vec);
			double scale = point.distance(start) / end.distance(start);
			vec = vec.multiply(calcLeafLength(scale));
			return point.add(vec);
		}
		
		
		void drawLeafVein(ArrayList<Point2D> nodes){
			// Use bezier to draw vein. Because it requires 4 points, we interpolate the inner 2 points.
			Point2D vecBack, vecForward;
			double t;
			Point2D p1, p2, p3;
			p1 = nodes.get(0).multiply(3);
			p2 = nodes.get(1).multiply(3);
			p3 = nodes.get(2);
			nodes.add(0, p1.subtract(p2).add(p3));          // add bogus point "point -1" thus we could use for loop equally to every points.
			p1 = nodes.get(nodes.size()-1).multiply(3);
			p2 = nodes.get(nodes.size()-2).multiply(3);
			p3 = nodes.get(nodes.size()-3);
			nodes.add(p1.subtract(p2).add(p3));             // add "point n+1"
			
			for(int i = 1;i < nodes.size() - 2; i++){
				vecBack = nodes.get(i).subtract(nodes.get(i-1)).normalize();
				vecForward = nodes.get(i+1).subtract(nodes.get(i+2)).normalize();
				t = 0.3 * nodes.get(i).distance(nodes.get(i+1));
				bezier(nodes.get(i),                                  // point i
						nodes.get(i).add(vecBack.multiply(t)),        // interpolation point
						nodes.get(i+1).add(vecForward.multiply(t)),   // interpolation point
						nodes.get(i+1));	                          // point i+1
			}
			nodes.remove(0);                      // remove the bogus point
			nodes.remove(nodes.size() - 1);
		}

		ArrayList<Point2D> buildNodes(Point2D start, Point2D end){
			ArrayList<Point2D> nodes = new ArrayList<Point2D>();
			nodes.add(start);
			for(int i = 1; i < nodesNum; i++){
				double t = calcDistribution(1.0*i / (nodesNum-1));
				Point2D startPart = start.multiply(1.0 - t);
				Point2D endPart = end.multiply(t);
				nodes.add(startPart.add(endPart));
			}
			for(int i = 1; i < nodesNum; i++){
				double degree = calcBendDegree(1.0*i / (nodesNum-1));
				Point2D vec = new Rotate(degree).transform(nodes.get(i).subtract(nodes.get(0)));
				nodes.set(i, nodes.get(0).add(vec));
			}
			return nodes;
		}
		
		double calcDistribution(double scale){
			// scale is the loop index's ratio, thus is, we assume index go from a small number to 1 (instead of 1 to n)
			// return value is the relative distance between that point(child leaf's root) and parent leaf's root.
//			return Math.sin(scale * Math.PI / 2.1);
			return Math.pow(scale , 0.7) ;
		}
		
		double calcBendDegree(double scale){
			// scale is the loop index's ratio, thus is, we assume index go from a small number to 1 (instead of 1 to n)
			// return value is the deviation angle between that point(child leaf's root) and the line from parent leaf's root to end.
			return 15 * (1 - scale);
		}

		double calcLeafLength(double scale){
			// scale is the relative value between the distance of child leaf root and parent leaf root,
			// and the distance of parent leaf's size.
			// return the relative value of child leaf's size and parent leaf's size.
			return (1 - scale) * 0.5;
		}

	}

//	class Streamline{
//		public Point2D location;
//		public Point2D direction;
//		Streamline(Point2D location, Point2D dirction){
//			this.location = location;
//			this.direction = direction;
//		}
//	}
	
//	class Point extends Point2D{
//		public Point(double x, double y){
//			super(x, y);
//		}
//		public Point rotate(double degrees){
//			double length = this.magnitude();
//			double radians = Math.toRadians(degrees) + Math.atan(this.getY() / this.getX());
//			return new Point(length*Math.cos(radians), length*Math.sin(radians));
//		}
//	}
	
}
