/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.dmn.model;

import java.util.ArrayList;
import java.util.List;

public class DmnDiEdge extends DmnDiDiagramElement {

    protected String dmnElementRef;
    protected String targetDmnElementRef;
    protected List<GraphicInfo> waypoints = new ArrayList<>();

    public String getDmnElementRef() {
        return dmnElementRef;
    }
    public void setDmnElementRef(String dmnElementRef) {
        this.dmnElementRef = dmnElementRef;
    }
    public String getTargetDmnElementRef() {
        return targetDmnElementRef;
    }
    public void setTargetDmnElementRef(String targetDmnElementRef) {
        this.targetDmnElementRef = targetDmnElementRef;
    }
    public void addWaypoint(GraphicInfo graphicInfo) {
        this.waypoints.add(graphicInfo);
    }
    public List<GraphicInfo> getWaypoints() {
        return waypoints;
    }
    public void setWaypoints(List<GraphicInfo> waypoints) {
        this.waypoints = waypoints;
    }
}
