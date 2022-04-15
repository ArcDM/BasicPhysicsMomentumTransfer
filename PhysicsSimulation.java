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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
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
    private static final Color ARROW_COLOR = Color.RED;
    private static final double BOX_SCALING = 100 / 5;
    private List< double[] > box_perameters;
    private List< Box > box_list;
    private Timer timer = null;
    private int collision_count;

    public PhysicsSimulation()
    {
        box_perameters = new ArrayList<>();
        box_perameters.add( new double[] { 200, 0, 100 } );
        box_perameters.add( new double[] { 400, -5, 100000000 } );

        box_list = new ArrayList<>();
        initializeboxes();

        timer = new Timer( 30, new Simulation() );

        getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( "P" ), "Pause_Button" );
        getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( "SPACE" ), "Pause_Button" );
        getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( "R" ), "R_Button" );

        getActionMap().put( "Pause_Button", new Pause() );
        getActionMap().put( "R_Button", new Reset() );

        JButton reset_button = new JButton( "Reset" );
        reset_button.getInputMap().put( KeyStroke.getKeyStroke( "SPACE" ), "none" ); // prevents the space bar from triggering the button (this *is* an issue otherwise)
        reset_button.addActionListener( new Reset() );

        JPanel panel = new JPanel();
        panel.add( reset_button );
        setLayout( new BorderLayout() );
        add( panel, BorderLayout.PAGE_START );
        panel.setBackground( BACKGROUND_COLOR );
        setBackground( BACKGROUND_COLOR );

        timer.start();
    }

    private class Simulation extends AbstractAction
    {
        @Override
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

            repaint();
        }
    }

    private class Pause extends AbstractAction
    {
        @Override
        public void actionPerformed( ActionEvent e )
        {
            if( timer.isRunning() )
            {
                timer.stop();
            }
            else
            {
                timer.start();
            }
        }
    }

    private class Reset extends AbstractAction
    {
        @Override
        public void actionPerformed( ActionEvent e )
        {
            initializeboxes();

            if( timer.isRunning() )
            {
                timer.restart();
            }
            else
            {
                repaint();
            }
        }
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

        Collections.sort( box_perameters, ( double[] v1, double[] v2 ) -> Double.compare( v1[ 0 ], v2[ 0 ] ) );
        // the boxes must be added in order of location so the internal value "collider" can be easily updated

        box_list.add( new Box(
            box_perameters.get( 0 )[ 0 ],
            box_perameters.get( 0 )[ 1 ],
            box_perameters.get( 0 )[ 2 ],
            null ) );

        for( int index = 1; index < box_perameters.size(); index++ )
        {
            box_list.add( new Box(
                box_perameters.get( index )[ 0 ],
                box_perameters.get( index )[ 1 ],
                box_perameters.get( index )[ 2 ],
                box_list.get( box_list.size() - 1 ) ) );
        }

        collision_count = 0;
    }

    @Override
    protected void paintComponent( Graphics gr )
    {
        super.paintComponent( gr );

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

        gr.drawLine( WALL_LOCATION / 2, GROUND_LOCATION, MAX_WIDTH, GROUND_LOCATION ); // horizontal ground line
        gr.drawLine( WALL_LOCATION, 0, WALL_LOCATION, GROUND_LOCATION ); // vertical wall line

        int double_line_constant = DECORATIVE_LINE_LENGTH * 2;

        for( int location = WALL_LOCATION / 2; location < MAX_WIDTH; location += double_line_constant )
        { // draw ground slanted decorative lines
            gr.drawLine(
                location,
                GROUND_LOCATION + DECORATIVE_LINE_LENGTH,
                location + DECORATIVE_LINE_LENGTH,
                GROUND_LOCATION );
        }

        for( int location = 0; location < MAX_WIDTH; location += 100 )
        { // draw distance units
            gr.drawString( String.format( "%d", location ),
                WALL_LOCATION + location - 5,
                GROUND_LOCATION + DECORATIVE_LINE_LENGTH + 10 );
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
            if( input_mass <= 0 )
            {
                throw new RuntimeException( "Mass must be greater than zero" );
            }

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
                gr.setColor( BOX_COLOR );

                gr.fillRect(
                    ( int ) Math.round( X_location ),
                    ( int ) Math.round( Y_location - size ),
                    ( int ) Math.round( size ),
                    ( int ) Math.round( size )
                );

                gr.setColor( ARROW_COLOR );

                if( velocity != 0 )
                {
                    int X_box_center = ( int ) Math.round( X_location + size / 2 ),
                        Y_box_center = ( int ) Math.round( Y_location - size / 2 ),
                        X_vector_end = ( int ) Math.round( X_location + size / 2 + velocity * BOX_SCALING / 2 ), // the "BOX_SCALING / 2" is arbitrary
                        X_arror_point = X_vector_end + ( ( velocity > 0 )? 5 : -5 ), // the "5" is arbitrary
                        line_length = ( int ) Math.round( 1 + Math.abs( velocity * BOX_SCALING / 2 ) ); // the "BOX_SCALING / 2" is arbitrary, the additonal "1" is to ensure visually joining with the arrow point

                    gr.drawString​( String.format( "v: %.2f", velocity ), X_box_center, Y_box_center );

                    gr.fillRect( // vector line
                        ( velocity > 0 )? X_box_center : X_vector_end - 1, // find the upper left point of the rectangle, the additonal "1" is to ensure visually joining with the arrow point
                        Y_box_center - 1, // the "1" is based on the thickness
                        line_length,
                        2 // the "2" is to make the rectangle barely thicker than drawLine()
                    );

                    gr.fillPolygon( // arrow point
                        new int[] { X_arror_point, X_vector_end, X_vector_end },
                        new int[] { Y_box_center, Y_box_center + 5, Y_box_center - 5 }, // the "5" is arbitrary
                        3 );
                }
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
