/* PhysicsSimulation.java

This implementation was initialy derived from an example found at
https://stackoverflow.com/questions/21868170/moving-objects-and-timers/21871022#21871022
*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
//import java.util.Random;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

@SuppressWarnings( "serial" )
public class PhysicsSimulation extends JPanel
{
    private static final int MAX_HEIGHT = 300;
    private static final int MAX_WIDTH = 600;
    private static final int GROUND_HEIGHT = 50;
    private static final int WALL_LOCATION = 10;
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color GROUND_COLOR = Color.ORANGE;
    private static final Color BOX_COLOR = Color.LIGHT_GRAY;
    private List< Box > boxes;
    private Timer timer = null;

    public PhysicsSimulation()
    {
        // initialize items
        boxes = new ArrayList<>();
        boxes.add( new Box( 500, 50, -200 ) );

        timer = new Timer( 30, new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                for( Box box : boxes )
                {
                    box.move();
                    repaint();
                    //System.out.println( "DEBUG: painted box" );
                }
                //System.out.println( "DEBUG: update" );
            }
        } );

        /*JButton start_button = new JButton( "Start" );
        start_button.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                timer.start();
            }
        } );*/

        JButton reset_button = new JButton( "Reset" );
        reset_button.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e ) {
                //boxes = createBoxList();
                timer.restart();
            }
        } );

        JPanel panel = new JPanel();
        //panel.add( start_button );
        panel.add( reset_button );
        setLayout( new BorderLayout() );
        add( panel, BorderLayout.PAGE_START );
        panel.setBackground( BACKGROUND_COLOR );
        setBackground( BACKGROUND_COLOR );

        timer.start();
    }

    @Override
    protected void paintComponent( Graphics gr )
    {
        super.paintComponent( gr );

        for( Box box : boxes )
        {
            box.drawBox( gr );
        }
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension( MAX_WIDTH, MAX_HEIGHT );
    }

    class Box
    {
        float X_location;
        float Y_location;
        float mass; // also displayed size
        float momentum; // signum designates direction
        boolean draw;

        public Box( float input_X, float input_mass, float input_momentum )
        {
            X_location = input_X;
            Y_location = GROUND_HEIGHT;
            mass = input_mass;
            momentum = input_momentum;
            draw = ( 0 - mass > X_location && X_location < MAX_WIDTH );
        }

        public void drawBox( Graphics gr )
        {
            if( draw )
            {
                gr.setColor( BOX_COLOR );

                gr.fillRect(
                    ( int ) Math.ceil( X_location ),
                    ( int ) Math.ceil( Y_location ) * 0,
                    ( int ) Math.ceil( mass ),
                    ( int ) Math.ceil( mass )
                );
            }
        }

        private float getVelocity()
        {
            return momentum / mass;
        }

        public void move()
        {
            X_location += getVelocity();

            draw = ( 0 - mass < X_location && X_location < MAX_WIDTH );
        }

        public boolean checkCollision()
        {
            return false;
        }

        public void resolveCollision()
        {

        }
    }
}
