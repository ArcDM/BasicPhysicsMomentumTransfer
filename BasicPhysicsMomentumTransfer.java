/* BasicPhysicsMomentumTransfer.java

This program simulates and animates blocks hitting each other on a frictionless 1D plane.
*/

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class BasicPhysicsMomentumTransfer
{
    public static void main( String args[] )
    {
        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                JFrame frame = new JFrame( "Basic Physics Momentum Transfer" );
                frame.add( new PhysicsSimulation() );
                frame.pack();
                frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
                frame.setLocationRelativeTo( null );
                frame.setVisible( true );
            }
        } );
    }
}
