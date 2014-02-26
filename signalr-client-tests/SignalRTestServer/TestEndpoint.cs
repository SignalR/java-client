using Microsoft.AspNet.SignalR;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SignalRTestServer
{
    public class TestEndpoint : PersistentConnection
    {
        public static String LastSentData;

        protected override System.Threading.Tasks.Task OnReceived(IRequest request, string connectionId, string data)
        {
            LastSentData = data;
            return base.OnReceived(request, connectionId, data);
        }
    }
}