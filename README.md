# NetworkControl
Only for MTK platform, minSdkVersion is 17.
MTK add an feature in NetworkManagementService after Android JB. NetworkManagementService provide an method setFirewallUidChainRule().
We can call setFirewallUidChainRule() to disable wifi and mobile network.
Just like goole code droidwall does, it's based on iptables.

Note:
    NetworkControl need framework.jar support, compile in eclipse or studio need library framework.jar
