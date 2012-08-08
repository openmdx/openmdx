/*
 * Copyright 2005 OpenXRI Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.openxri;


/*
********************************************************************************
* Interface: XRIReference
********************************************************************************
*/ /**
* This interface is implemented by all valid XRIReference classes.
*
* @author =chetan
*/

// TBD: implement this class
public interface XRIReference
{
    /*
    ****************************************************************************
    * getAuthorityPath()
    ****************************************************************************
    */ /**
    * Returns the AuthorityPath component of this XRI Reference
    */
    AuthorityPath getAuthorityPath();

    /*
    ****************************************************************************
    * getXRIPath()
    ****************************************************************************
    */ /**
    *Returns the XRI Path component of this XRI Reference
    */
    XRIPath getXRIPath();

} // Interface: XRIReference
