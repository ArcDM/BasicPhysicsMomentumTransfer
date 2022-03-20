/* PhysicsSimulation.java

This implementation was initialy derived from an example found at
https://stackoverflow.com/questions/21868170/moving-objects-and-timers/21871022#21871022
and inspired by a 3Blue1Brown video on the subject at
https://www.youtube.com/watch?v=jsYwFizhncE
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
    private static final int GROUND_LOCATION = MAX_HEIGHT - 30;
    private static final int WALL_LOCATION = 30;
    private static final int DECORATIVE_LINE_LENGTH = 10;
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color GROUND_COLOR = Color.ORANGE;
    private static final Color BOX_COLOR = Color.LIGHT_GRAY;
    private List< Box > boxes;
    private Timer timer = null;

    public PhysicsSimulation()
    {
        boxes = new ArrayList<>();
        initializeBoxes();

        timer = new Timer( 30, new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                for( double change_in_time, impulse_time = 0.0; impulse_time < 1; impulse_time += change_in_time )
                {
                    change_in_time = 1 - impulse_time;

                    for( Box box : boxes )
                    { // time only advances to the next collision
                        change_in_time = Math.min( box.checkCollision(), change_in_time );
                    }

                    if( change_in_time <= 0 )
                    {
                        System.out.printf( "FATAL ERROR change_in_time is less than zero: %f\n", change_in_time );
                        debugBoxes();

                        System.exit( 1 );
                    }

                    for( Box box : boxes )
                    {
                        box.move( change_in_time );
                    }

                    for( int index = boxes.size() - 1; index >= 0; index-- )
                    { // boxes can chain collide, there is a need to check the other boxes
                        if( boxes.get( index ).resolveCollision() )
                        {
                            index += ( index == boxes.size() - 1 )? 1 : 2;
                        }
                    }
                }

                for( Box box : boxes )
                {
                    repaint();
                }
            }
        } );

        JButton reset_button = new JButton( "Reset" );
        reset_button.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e ) {
                initializeBoxes();
                timer.restart();
            }
        } );

        JPanel panel = new JPanel();
        panel.add( reset_button );
        setLayout( new BorderLayout() );
        add( panel, BorderLayout.PAGE_START );
        panel.setBackground( BACKGROUND_COLOR );
        setBackground( BACKGROUND_COLOR );

        timer.start();
    }

    private void debugBoxes()
    {
        for( Box box : boxes )
        {
            System.out.printf( "DEBUG:\tBox %d location %f, offset %f, velocity %f\n",
                box.index, box.X_location, box.X_location + box.size, box.velocity );
        }
    }

    private void initializeBoxes()
    {
        boxes.clear();
        boxes.add( new Box( 100, 5, 1000 ) );
        boxes.add( new Box( 200, 0, 1000 ) );
        boxes.add( new Box( 300, -5, 1000 ) );
    }

    @Override
    protected void paintComponent( Graphics gr )
    {
        super.paintComponent( gr );

        gr.setColor( BOX_COLOR );

        for( Box box : boxes )
        {
            box.drawBox( gr );
        }

        drawGround( gr );
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension( MAX_WIDTH, MAX_HEIGHT );
    }

    private void drawGround( Graphics gr )
    {
        gr.setColor( GROUND_COLOR );

        gr.drawLine( WALL_LOCATION >> 1, GROUND_LOCATION, MAX_WIDTH, GROUND_LOCATION ); // horizontal ground line
        gr.drawLine( WALL_LOCATION, 0, WALL_LOCATION, GROUND_LOCATION ); // vertical wall line

        int double_line_constant = DECORATIVE_LINE_LENGTH << 1;

        for( int location = WALL_LOCATION >> 1; location < MAX_WIDTH; location += double_line_constant )
        { // draw ground slanted decorative lines
            gr.drawLine(
                location,
                GROUND_LOCATION + DECORATIVE_LINE_LENGTH,
                location + DECORATIVE_LINE_LENGTH,
                GROUND_LOCATION);
        }

        for( int location = GROUND_LOCATION - double_line_constant; location > double_line_constant; location -= double_line_constant )
        { // draw wall slanted decorative lines
            gr.drawLine(
                WALL_LOCATION - DECORATIVE_LINE_LENGTH,
                location,
                WALL_LOCATION,
                location - DECORATIVE_LINE_LENGTH );
        }
    }

    class Box
    {
        double X_location;
        double Y_location;
        double velocity; // signum designates direction
        double mass;
        double size;
        int index;

        public Box( double input_X, double input_velocity, double input_mass )
        {
            X_location = input_X;
            Y_location = GROUND_LOCATION;
            velocity = input_velocity;
            mass = input_mass;
            size = Math.sqrt( mass );
            index = boxes.size();
        }

        public void drawBox( Graphics gr )
        {
            if( 0 - size < X_location && X_location < MAX_WIDTH )
            { // draw if can be seen
                gr.fillRect(
                    ( int ) Math.round( X_location ),
                    ( int ) Math.round( Y_location - size ),
                    ( int ) Math.round( size ),
                    ( int ) Math.round( size )
                );
            }
        }

        public void move( double movement_ratio )
        {
            X_location += velocity * movement_ratio;
        }

        public double checkCollision()
        { // returns time it takes to collide were the time is number of cycles needed
            double return_value;

            if( index == 0 )
            { // how long until the box would hit the wall
                return_value = ( WALL_LOCATION - X_location ) / velocity;
            }
            else
            { // how long until the box would hit the box to the left
                return_value = ( boxes.get( index - 1 ).X_location + boxes.get( index - 1 ).size - X_location ) / ( velocity - boxes.get( index - 1 ).velocity );
            }

            return return_value > 0? return_value : 1.0;
        }

        public boolean resolveCollision()
        {
            if( index == 0 && X_location == WALL_LOCATION && velocity < 0 )
            { // hits wall
                velocity *= -1;
                return true;
            }
            else if( index > 0 && boxes.get( index - 1 ).X_location + boxes.get( index - 1 ).size - X_location == 0 && ( boxes.get( index - 1 ).velocity > 0 || velocity < 0 ) )
            { // hits box to left
                double collider_velocity = boxes.get( index - 1 ).velocity,
                        collider_mass = boxes.get( index - 1 ).mass;

                /* initial velocities (v) to final velocities (u)
                    m1 * v1 + m2 * v2 = m1 * u1 + m2 * u2
                    u1 = ( v1 * ( m1 - m2 ) + 2 * m2 * v2 ) / ( m1 + m2 ) */

                boxes.get( index - 1 ).velocity = ( collider_velocity * ( collider_mass - mass ) + 2 * mass * velocity ) / ( mass + collider_mass );

                velocity = ( velocity * ( mass - collider_mass ) + 2 * collider_mass * collider_velocity ) / ( mass + collider_mass );

                return true;
            }

            return false;
        }
    }
}
