using UnityEngine;
using System.Collections;

public class MessageHandler : MonoBehaviour
{
    public string Response;
    void Start()
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        AndroidJNIHelper.debug = true;
        using (AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
        {
            jc.CallStatic("UnitySendMessage", "MessageHandler", "JavaMessage", "whoowhoo");
        }
#endif
    }

    void JavaMessage(string message)
    {
        Debug.Log("got start message from java: " + message);
    }

    void HandleText(string message)
    {
        Debug.Log("got message from Phone: " + message);
        Response += message;
    }
    
    /// <summary>
    /// Can be used by NGUI
    /// </summary>
    public void CreateBandTile()
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        Debug.Log("making a class");
        // to get the activity
        using (var jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
        {
            using (var jo = jc.GetStatic<AndroidJavaObject>("currentActivity"))
            {
                Debug.Log("going to execute");
                jo.Call("RunButtonTask");
            }
        }
#elif UNITY_EDITOR
        Response = "Pressed CreateBand";
#endif

    }


}
