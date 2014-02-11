using System;
using System.Web;
using Microsoft.AspNet.SignalR;
namespace SignalRTestServer
{
    public class TestHub : Hub
    {
        public static String LastSentData = null;

        public void Send(string name, string message)
        {
            // Call the broadcastMessage method to update clients.
            Clients.All.broadcastMessage(name, message);
        }

        public void TestMethod(String data)
        {
            LastSentData = data;
        }
    }
}