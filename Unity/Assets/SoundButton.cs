using UnityEngine;
using System.Collections;

public class SoundButton : MonoBehaviour
{

    public MessageHandler m;

    void OnGUI()
    {
        // Make the first button. If it is pressed, Application.Loadlevel (1) will be executed
        if (GUI.Button(new Rect(500, 100, 300, 20), "Make MS Band Tile for sound"))
        {
            CreateBandTile();
        }

        GUI.Label(new Rect(500, 150, 1000, 1000), m.Response);
    }

    // Use this for initialization
    void Start()
    {
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
            mainActivity.Call("RunButtonTask");
#endif
        }
        else
        {
            m.Response = "Pressed CreateBand";
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
            m.Response = "Pressed Volume Up";
        }

    }
}
