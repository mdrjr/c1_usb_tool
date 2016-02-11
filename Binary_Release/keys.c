//------------------------------------------------------------------------------------------------------------
//
// ADS7846 TFT-LCD Board Keypad Application. (Use wiringPi Library)
// Defined port number is wiringPi port number.
//
// Compile : gcc -o <create excute file name> <source file name> -lwiringPi -lwiringPiDev -lpthread
// Run : sudo ./<created excute file name>
//
//------------------------------------------------------------------------------------------------------------
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
 
#include <unistd.h>
#include <string.h>
#include <time.h>
 
#include <wiringPi.h>
#include <wiringPiI2C.h>
#include <wiringSerial.h>
 
#include <fcntl.h>
#include <linux/input.h>
#include <linux/uinput.h>
 
//------------------------------------------------------------------------------------------------------------
//
// Global handle Define
//
//------------------------------------------------------------------------------------------------------------
#define KEY_PRESS   1
#define KEY_RELEASE 0
 
#define HIGH        1
#define LOW         0
 
#define UPDATE_PERIOD   200 // 200ms
 
//------------------------------------------------------------------------------------------------------------
//
// UINPUT Handle:
//
//------------------------------------------------------------------------------------------------------------
int uInputFd = -1;
 
#define UINPUT_DEV_NODE "/dev/uinput"
 
//------------------------------------------------------------------------------------------------------------
//
// Keypad Define:
//
//------------------------------------------------------------------------------------------------------------
#define PORT_KEY1   1   // GPIOY.BIT7(#87)
#define PORT_KEY2   4   // GPIOX.BIT7(#014)
#define PORT_KEY3   5   // GPIOX.BIT5(#102)
 
struct  {
    unsigned char   port;       // GPIO Number
    unsigned char   def_status; // default release status (port HIGH or LOW)
    char            code;       // Keycode
    int             status;     // current status
}   UserKey[] = {
    {   PORT_KEY1, HIGH, KEY_UP, KEY_RELEASE },
    {   PORT_KEY2, HIGH, KEY_DOWN, KEY_RELEASE },
    {   PORT_KEY3, HIGH, KEY_ENTER, KEY_RELEASE },
};
 
#define MAX_KEY_CNT     sizeof(UserKey)/sizeof(UserKey[0])
 
//------------------------------------------------------------------------------------------------------------
//------------------------------------------------------------------------------------------------------------
//
// Keypad Event Send Function:
//
//------------------------------------------------------------------------------------------------------------
static int send_key_event (int fd, unsigned int type, unsigned int code, unsigned int value)
{
    struct  input_event event;
 
    memset(&event, 0, sizeof(event));
 
    event.type  = type;
    event.code  = code;
    event.value = value;
 
    if(write(fd, &event, sizeof(event)) != sizeof(event))    {
        fprintf(stderr, "%s : event send error!!\n", __func__);
        return  -1;
    }
 
    return 0;
}
 
//------------------------------------------------------------------------------------------------------------
//
// Keypad Update Function:
//
//------------------------------------------------------------------------------------------------------------
static void key_update (void)
{
    int i;
 
    for(i = 0; i < MAX_KEY_CNT; i++)    {
        if(digitalRead(UserKey[i].port) != UserKey[i].status)   {
 
            UserKey[i].status = digitalRead(UserKey[i].port);
            send_key_event( uInputFd, 
                            EV_KEY,
                            UserKey[i].code,
                            UserKey[i].status != UserKey[i].def_status ? KEY_PRESS : KEY_RELEASE );
            send_key_event( uInputFd, 
                            EV_SYN,
                            SYN_REPORT,
                            0 );
        }
    }
}
 
//------------------------------------------------------------------------------------------------------------
//
// system init
//
//------------------------------------------------------------------------------------------------------------
int system_init(void)
{
    int i;
    struct uinput_user_dev  device;
 
    memset(&device, 0, sizeof(device));
 
    if((uInputFd = open(UINPUT_DEV_NODE, O_WRONLY)) < 0)    {
        fprintf(stderr, "%s : %s Open error!\n", __func__, UINPUT_DEV_NODE);
        return  -1;
    }
 
    // uinput device init
    strcpy(device.name, "keypad");
    device.id.bustype   = BUS_HOST;
    device.id.vendor    = 0x16B4;
    device.id.product   = 0x0701;
    device.id.version   = 0x0001;
 
    if(write(uInputFd, &device, sizeof(device)) != sizeof(device))   {
        fprintf(stderr, "%s : device init error!\n", __func__);
        goto    err_out;
    }
 
    if(ioctl(uInputFd, UI_SET_EVBIT, EV_KEY) < 0)   {
        fprintf(stderr, "%s : evbit set error!\n", __func__);
        goto    err_out;
    }
 
    // Keypad port init
    for(i = 0; i < MAX_KEY_CNT; i++)    {
        pinMode (UserKey[i].port, INPUT);
        pullUpDnControl (UserKey[i].port, PUD_UP);
 
        if(ioctl(uInputFd, UI_SET_KEYBIT, UserKey[i].code) < 0) {
            fprintf(stderr, "%s : keybit set error!\n", __func__);
            goto    err_out;
        }
    }
 
    if(ioctl(uInputFd, UI_DEV_CREATE) < 0)  {
        fprintf(stderr, "%s : dev create error!\n", __func__);
        goto    err_out;
    }
 
    return  0;
 
err_out:
    close(uInputFd);    uInputFd = -1;    
 
    return  -1;
 }
 
//------------------------------------------------------------------------------------------------------------
//
// Start Program
//
//------------------------------------------------------------------------------------------------------------
int main (int argc, char *argv[])
{
    static int timer = 0 ;
 
    wiringPiSetup ();
 
    if (system_init() < 0)
    {
        fprintf (stderr, "%s: System Init failed\n", __func__);     return -1;
    }
 
    for(;;)    {
        usleep(100000);
        if (millis () < timer)  continue ;
 
        timer = millis () + UPDATE_PERIOD;
 
        // All Data update
        key_update();
    }
 
    if(uInputFd)    close(uInputFd);
 
    return 0 ;
}
 
//------------------------------------------------------------------------------------------------------------
//------------------------------------------------------------------------------------------------------------

