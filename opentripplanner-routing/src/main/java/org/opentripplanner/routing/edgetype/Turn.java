/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.edgetype;

import org.opentripplanner.common.geometry.DirectionUtils;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.AbstractEdge;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;

import com.vividsolutions.jts.geom.Geometry;

public class Turn extends AbstractEdge {

    private static final long serialVersionUID = 5348819949436171603L;

    public int turnAngle;

    public Turn(Edge in, Edge out) {
        super(in.getToVertex(), out.getFromVertex());

        double angleDiff = Math.abs(DirectionUtils.getInstance().getLastAngle(in.getGeometry())
                - DirectionUtils.getInstance().getFirstAngle(out.getGeometry()));
        turnAngle = (int) (180 * angleDiff / Math.PI);
        if (turnAngle > 180) {
            turnAngle = 360 - turnAngle;
        }
    }

    @Override
    public String getDirection() {
        return null;
    }

    @Override
    public double getDistance() {
        return 0;
    }

    @Override
    public Geometry getGeometry() {
        return null;
    }

    @Override
    public TraverseMode getMode() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public TraverseResult traverse(State s0, TraverseOptions wo) throws NegativeWeightException {
        State s1 = s0.clone();
        s1.incrementTimeInSeconds(turnAngle / 20);
        return new TraverseResult(turnAngle / 20.0, s1);
    }

    @Override
    public TraverseResult traverseBack(State s0, TraverseOptions wo) throws NegativeWeightException {
        State s1 = s0.clone();
        s1.incrementTimeInSeconds(-turnAngle / 20);
        return new TraverseResult(turnAngle / 20.0, s1);
    }

}