#define RENDERSCREEN_MODULE
#include <Python.h>
#include </usr/include/python3.12/pyconfig-64.h>
#include <GL/glew.h>
#include <GLFW/glfw3.h>

void Framebuffer_Size_Callback(GLFWwindow* window, int width, int height) {
    glViewport(0,0, width, height);
}

PyObject* Render_Screen(PyObject* self, PyObject* args) {
    
    PyObject* Fail = PyLong_FromLong(-1);
    PyObject* Success = PyLong_FromLong(0);
    
    GLFWwindow* window;
    
    /* Initialize the library */
    if (!glfwInit())
        return Fail;
    
    int width;
    int height;
    Py_buffer buffer;
    //const char* data;
    if (!PyArg_ParseTuple(args, "iiy*", &width, &height, &buffer))
        return NULL;

    unsigned char* data = (unsigned char*)buffer.buf;
    //printf(data);
    /* Create a windowed mode window and its OpenGL context */
    //glScalef(1, -1, 1);
    int windowWidth = width / 2;
    int windowHeight = height / 2;
    window = glfwCreateWindow(windowWidth, windowHeight, "Phone Display", NULL, NULL);
    if (!window)
    {
        glfwTerminate();
        return Fail;
    }

    /* Make the window's context current */
    glfwMakeContextCurrent(window);
    //glScalef(1, -1, 1);
    //int counter = 0;
    /* Loop until the user closes the window */
    Py_BEGIN_ALLOW_THREADS
    while (!glfwWindowShouldClose(window))
    {
        //fprintf(stdout, data);
        /* Render here */
        glClear(GL_COLOR_BUFFER_BIT);
        glRasterPos2f(-1,1);
        glPixelZoom(1, -1);
        
        /*
        unsigned int* texID;
        glGenTextures (1, texID);
        glBindTextures (GL_TEXTURE_RECTANGLE_EXT, 1, texID);
        glTexImage2D (GL_TEXTURE_RECTANGLE_EXT, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        glBegin (GL_QUADS);
        glTexCoord2f (0, 0);
        glVertex2f (0, 0);
        glTexCoord2f (width, 0);
        glVertex2f (windowWidth, 0);
        glTexCoord2f (width, height);
        glVertex2f (windowWidth, windowHeight);
        glTexCoord2f (0, height);
        glVertex2f (0, windowHeight);
        glEnd();
        */

        glDrawPixels(width, height, GL_RGBA, GL_UNSIGNED_BYTE, data);
        /* Swap front and back buffers */
        glfwSwapBuffers(window);

        /* Poll for and process events */
        glfwPollEvents();
    }
    glfwTerminate();
    Py_END_ALLOW_THREADS
    return Success;
}

PyMethodDef RenderMethods[] = {
    {"Render_Screen", Render_Screen, METH_VARARGS, "Render the OpenGL Display"},
    {NULL, NULL, 0, NULL}
};

static struct PyModuleDef RenderScreen =
{
    PyModuleDef_HEAD_INIT,
    "RenderScreen", /* name of module */
    "",          /* module documentation, may be NULL */
    -1,          /* size of per-interpreter state of the module, or -1 if the module keeps state in global variables. */
    RenderMethods
};

PyMODINIT_FUNC PyInit_RenderScreen(void) {
    return PyModule_Create(&RenderScreen);
}

int main(int argc, char *argv[]) {
    PyStatus status;
    PyConfig config;
    PyConfig_InitPythonConfig(&config);

    /* Add a built-in module, before Py_Initialize */
    if (PyImport_AppendInittab("RenderScreen", PyInit_RenderScreen) == -1) {
        fprintf(stderr, "Error: could not extend in-built modules table\n");
        exit(1);
    }

    /* Pass argv[0] to the Python interpreter */
    status = PyConfig_SetBytesString(&config, &config.program_name, argv[0]);
    if (PyStatus_Exception(status)) {
        goto exception;
    }

    /* Initialize the Python interpreter.  Required.
       If this step fails, it will be a fatal error. */
    status = Py_InitializeFromConfig(&config);
    if (PyStatus_Exception(status)) {
        goto exception;
    }
    PyConfig_Clear(&config);

    /* Optionally import the module; alternatively,
       import can be deferred until the embedded script
       imports it. */
    PyObject *pmodule = PyImport_ImportModule("RenderScreen");
    if (!pmodule) {
        PyErr_Print();
        fprintf(stderr, "Error: could not import module 'RenderScreen'\n");
    }

    // ... use Python C API here ...

    return 0;

  exception:
     PyConfig_Clear(&config);
     Py_ExitStatusException(status);
}
