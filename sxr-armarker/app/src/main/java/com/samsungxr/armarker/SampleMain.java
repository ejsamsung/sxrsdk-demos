/* Copyright 2015 Samsung Electronics Co., LTD
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

package com.samsungxr.armarker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.joml.Vector3f;
import java.io.IOException;
import java.io.InputStream;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.mixedreality.IMarkerEvents;
import com.samsungxr.mixedreality.SXRMarker;
import com.samsungxr.mixedreality.SXRMixedReality;
import com.samsungxr.mixedreality.SXRTrackingState;
import com.samsungxr.nodes.SXRCubeNode;


/**
 * This sample illustrates how to load, place and move a 3D model
 * on a plane in the real world.
 */
public class SampleMain extends SXRMain {
    private static String TAG = "ARMARKER";

    private SXRContext mSXRContext;
    private SXRScene mainScene;
    private SXRMixedReality mixedReality;
    private SXRDirectLight mSceneLight;
    private SXRNode mAnchor = null;

    /**
     * Initialize the MixedReality extension and
     * provide it with listeners for marker detection
     * and anchor tracking.
     *
     * A headlight is put in the scene to illuminate
     * objects the camera is pointed at.
     */
    @Override
    public void onInit(SXRContext ctx)
    {
        mSXRContext = ctx;
        mainScene = mSXRContext.getMainScene();
        mSceneLight = new SXRDirectLight(ctx);
        mainScene.getMainCameraRig().setNearClippingDistance(0.1f);
        mainScene.getMainCameraRig().setFarClippingDistance(100);
        mainScene.getMainCameraRig().getHeadTransformObject().attachComponent(mSceneLight);
        mixedReality = new SXRMixedReality(mainScene);
        mixedReality.setARToVRScale(1.0f);
        mixedReality.getEventReceiver().addListener(markerListener);
        mixedReality.resume();
        addMarker("chips.jpg");
    }


    /**
     * The mixed reality extension runs in the background and does
     * light estimation. Each frame the intensity of the ambient
     * lighting is adjusted based on that estimate.
     */
    @Override
    public void onStep()
    {
        super.onStep();
        float lightEstimate = mixedReality.getLightEstimate().getPixelIntensity();
        mSceneLight.setAmbientIntensity(lightEstimate, lightEstimate, lightEstimate, 1);
        mSceneLight.setDiffuseIntensity(0.4f, 0.4f, 0.4f, 1);
        mSceneLight.setSpecularIntensity(0.2f, 0.2f, 0.2f, 1);
    }


    /**
     * Create a tall box and place it in the virtual world
     * at the marker position. We make sure the bottom of the
     * box is at (0,0,0) or tracking will not work properly.
     * We create an anchor (and a corresponding
     * node) to link the real and virtual pose together.
     * The node attached to the anchor will be moved and
     * oriented by the framework.
     */
    public void addVirtualObject(SXRMarker marker)
    {
        SXRMaterial mtl = new SXRMaterial(mSXRContext, SXRMaterial.SXRShaderType.Phong.ID);
        SXRNode object = new SXRCubeNode(mSXRContext, true, mtl, new Vector3f(0.1f, 1.6f, 0.1f));

        object.getTransform().setPositionY(0.8f);
        mAnchor = new SXRNode(mSXRContext);
        mtl.setDiffuseColor(0, 1, 0.8f, 1);
        marker.createAnchor(mAnchor);
        mAnchor.addChildObject(object);
        mainScene.addNode(mAnchor);
    }

    /**
     * Add a bitmap file to the list of recognized markers.
     * @param filename name of bitmap file
     */
    public void addMarker(String filename)
    {
        try
        {
            InputStream stream = mSXRContext.getContext().getAssets().open(filename);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            if (bitmap != null)
            {
                mixedReality.addMarker(filename, bitmap);
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }


    /**
     * Add a virtual object when the marker starts tracking.
     * Hide it when the marker is not tracking.
     */
    private IMarkerEvents markerListener = new IMarkerEvents()
    {
        public void onMarkerDetected(SXRMarker marker) { }

        public void onMarkerStateChange(SXRMarker marker, SXRTrackingState state)
        {
            if (state == SXRTrackingState.TRACKING)
            {
                if (mAnchor == null)
                {
                    addVirtualObject(marker);
                }
            }
        }

    };

}