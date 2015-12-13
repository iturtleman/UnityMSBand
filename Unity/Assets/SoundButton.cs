using UnityEngine;
using System.Collections;
using System;

public class SoundButton : MonoBehaviour
{

    public MessageHandler m;

    public static void ScaleBasedOffOriginalSize(int screenWidth, int screenHeight)
    {
        Vector2 resizeRatio = new Vector2((float)Screen.width / screenWidth, (float)Screen.height / screenHeight);
        GUI.matrix = Matrix4x4.TRS(Vector3.zero, Quaternion.identity, new Vector3(resizeRatio.x, resizeRatio.y, 1.0f));
    }

    void OnGUI()
    {
        ScaleBasedOffOriginalSize(800, 600);
        // Make the first button. If it is pressed, Application.Loadlevel (1) will be executed
        if (GUI.Button(new Rect(300, 100, 300, 20), "Make MS Band Tile for sound"))
        {
            CreateBandTile();
        }

        if (GUI.Button(new Rect(100, 160, 100, 20), "Delete all tiles"))
        {
            RemoveAllBandTiles();
        }

        if (GUI.Button(new Rect(100, 120, 100, 20), "Volume Up"))
        {
            VolumeUp();
        }
        if (GUI.Button(new Rect(100, 140, 100, 20), "Volume Down"))
        {
            VolumeDown();
        }

        if (GUI.Button(new Rect(100, 200, 100, 20), "Clear Responses"))
        {
            m.Response = string.Empty;
        }

        GUI.Label(new Rect(300, 150, 1000, 1000), m.Response);
    }

    // Use this for initialization
    void Start()
    {
        m.Response = string.Format("Screen Size {0} x {1}", Screen.width, Screen.height);
        if (!Application.isEditor)
        {
#if UNITY_ANDROID
            Debug.Log("making a class");
            // to get the activity
            mainActivity = new AndroidJavaClass("com.unity3d.player.UnityPlayer").GetStatic<AndroidJavaObject>("currentActivity");
#endif
        }
    }

    // Update is called once per frame
    void Update()
    {

    }
#if UNITY_ANDROID
    AndroidJavaObject mainActivity;
#endif

    public void CreateBandTile()
    {
        if (!Application.isEditor)
        {
#if UNITY_ANDROID
            Debug.Log("going to execute");
            mainActivity.Call("CreateBandTile");
#endif
        }
        else
        {
            m.Response = "Pressed CreateBand";
        }
    }

    public void RemoveAllBandTiles()
    {
        if (!Application.isEditor)
        {
#if UNITY_ANDROID
            Debug.Log("going to execute");
            //init band client so we can delete (handles exist in java)
            if (mainActivity.Call<bool>("getConnectedBandClient"))
            {
                using (AndroidJavaObject client = mainActivity.Get<AndroidJavaObject>("client"))
                {
                    Debug.Log("making a uuid");
                    //ideally you'd want to do all this in java code so you aren't doing this, but it's a good way to show how you would use it
                    //you can also get the field from the java class
                    using (var uuid = new AndroidJavaClass("java.util.UUID"))
                    {
                        using (var guid = uuid.CallStatic<AndroidJavaObject>("fromString", "cc0D508F-70A3-47D4-BBA3-812BADB1F8Aa"))
                        {
                            try
                            {
                                Debug.Log("Get tilemanager");
                                //this is stupidly inefficient but shows off nested function calls
                                var tileman = client.Call<AndroidJavaObject>("getTileManager");
                                Debug.Log("remove tile attempt");
                                var res = tileman.Call<AndroidJavaObject>("removeTile", guid);
                                Debug.Log(res);
                                Debug.Log(res.ToString());
                            }
                            catch (Exception e)
                            {
                                Debug.Log(e);
                                mainActivity.Call("RemoveBandTiles");
                            }
                        }
                    }
                }
            }

#endif
        }
        else
        {
            m.Response = "Pressed DeleteBandTiles";
        }
    }

    public void VolumeUp()
    {
        if (!Application.isEditor)
        {
#if UNITY_ANDROID
            Debug.Log("Volume Up");
            mainActivity.Call("VolumeUp");
#endif
        }
        else
        {
            m.Response = "Pressed Volume Up";
        }

    }

    public void VolumeDown()
    {
        if (!Application.isEditor)
        {
#if UNITY_ANDROID
            Debug.Log("Volume Down");
            mainActivity.Call("VolumeDown");
#endif
        }
        else
        {
            m.Response = "Pressed Volume Down";
        }

    }
}
