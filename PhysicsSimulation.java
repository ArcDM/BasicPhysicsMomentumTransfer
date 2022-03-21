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
import java.util.Iterator;
import java.util.NoSuchElementException;
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
    private static final double BOX_SCALING = 100 / 5;
    private List< Box > box_list;
    private Timer timer = null;

    int collision_count;

    public PhysicsSimulation()
    {
        box_list = new ArrayList<>();
        initializeboxes();

        timer = new Timer( 30, new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                for( double change_in_time, impulse_limit = 1.0; impulse_limit > 0.0; impulse_limit -= change_in_time )
                { // checks and resolves all physics interactions whithin the time frame
                    Box collision_reference = null;
                    change_in_time = impulse_limit;

                    for( Box box : box_list )
                    { // time only advances to the next collision
                        double collision_time = box.getCollisionTime();

                        if( collision_time >= 0.0 && change_in_time > collision_time )
                        {
                            change_in_time = collision_time;
                            collision_reference = box;
                        }
                    }

                    for( Box box : box_list )
                    {
                        box.move( change_in_time );
                    }

                    if( collision_reference != null )
                    {
                        collision_count++;
                        collision_reference.resolveCollision();
                        //System.out.printf( "DEBUG: Box %d collided, collision_count %d\n", box_list.indexOf( collision_reference ), collision_count );
                    }
                }

                for( Box box : box_list )
                {
                    repaint();
                }
            }
        } );

        JButton reset_button = new JButton( "Reset" );
        reset_button.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e ) {
                initializeboxes();
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

    private void debugboxes()
    {
        for( Box box : box_list )
        {
            box.printDebug();
        }
    }

    private void initializeboxes()
    {
        box_list.clear();
        box_list.add( new Box( 200, 0, 100, null ) );
        box_list.add( new Box( 400, -5, 100000000, box_list.get( box_list.size() - 1 ) ) );

        collision_count = 0;
    }

    @Override
    protected void paintComponent( Graphics gr )
    {
        super.paintComponent( gr );

        gr.setColor( BOX_COLOR );

        for( Box box : box_list )
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
        private double X_location;
        private double Y_location;
        private double velocity; // signum designates direction
        private double mass;
        private double size;
        private Box collider; // the box to the left, if any

        public Box( double input_X, double input_velocity, double input_mass, Box input_collider )
        {
            X_location = input_X;
            Y_location = GROUND_LOCATION;
            velocity = input_velocity;
            mass = input_mass;
            size = Math.max( Math.log10( mass ), 1 ) * BOX_SCALING;
            collider = input_collider;
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

        public double getCollisionTime()
        { // returns time it takes to collide were the time is number of cycles needed
            if( collider == null )
            { // how long until the box would hit the wall
                if( velocity < 0 )
                {
                    return ( WALL_LOCATION - X_location ) / velocity;
                }
            }
            else if( collider.velocity > velocity )
            { // how long until the box would hit the box to the left
                return ( collider.X_location + collider.size - X_location ) / ( velocity - collider.velocity );
            }

            return Double.NaN; // not on a collision course
        }

        public void resolveCollision()
        {
            if( collider == null )
            { // hits wall
                velocity *= -1;
            }
            else
            { // hits box to left
                /* initial velocities (v) to final velocities (u)
                    m1 * v1 + m2 * v2 = m1 * u1 + m2 * u2
                    u1 = ( v1 * ( m1 - m2 ) + 2 * m2 * v2 ) / ( m1 + m2 ) */

                double new_velocity = ( velocity * ( mass - collider.mass ) + 2 * collider.mass * collider.velocity ) / ( mass + collider.mass );

                collider.velocity = ( collider.velocity * ( collider.mass - mass ) + 2 * mass * velocity ) / ( mass + collider.mass );

                velocity = new_velocity;
            }
        }

        public void printDebug()
        {
            System.out.printf( "DEBUG:\tBox %d location %f, offset %f, velocity %f\n",
                box_list.indexOf( this ), X_location, X_location + size, velocity );
        }
    }
}
