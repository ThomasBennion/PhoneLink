#define RENDERSCREEN_MODULE
#include <Python.h>
#include </usr/include/python3.12/pyconfig-64.h>
#include <GL/glew.h>
#include <GLFW/glfw3.h>

void Framebuffer_Size_Callback(GLFWwindow* window, int width, int height) {
    glViewport(0,0, width, height);
}

char* loadShader(const char* filePath) {
    FILE* file = fopen(filePath, "rb");
    if (!file) {
        fprintf(stderr, "Unable to open file '%s'\n", filePath);
        return NULL;
    }

    fseek(file, 0, SEEK_END);
    long length = ftell(file);
    fseek(file, 0, SEEK_SET);

    char* source = (char*)malloc(length + 1);
    fread(source, 1, length, file);
    source[length] = '\0';

    fclose(file);
    return source;
}

GLuint compileShader(const char* filePath, GLenum shaderType) {
    char* shaderSource = loadShader(filePath);
    
    if (!shaderSource) {
        return 0;
    }

    GLuint shader = glCreateShader(shaderType);
    
    glShaderSource(shader, 1, (const GLchar* const*)&shaderSource, NULL);
    glCompileShader(shader);

    GLint success;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetShaderInfoLog(shader, 512, NULL, infoLog);
        fprintf(stderr, "Shader compilation failed: '%s'\n", infoLog);
    }

    free(shaderSource);
    
    return shader;
}

GLuint createShaderProgram(const char* vertexPath, const char* fragmentPath) {
    GLuint vertexShader = compileShader(vertexPath, GL_VERTEX_SHADER);
    GLuint fragmentShader = compileShader(fragmentPath, GL_FRAGMENT_SHADER);
    
    GLuint shaderProgram = glCreateProgram();
    glAttachShader(shaderProgram, vertexShader);
    glAttachShader(shaderProgram, fragmentShader);
    glLinkProgram(shaderProgram);

    GLint success;
    glGetProgramiv(shaderProgram, GL_LINK_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetProgramInfoLog(shaderProgram, 512, NULL, infoLog);
        fprintf(stderr, "Program linking failed '%s'\n", infoLog);
    }

    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);

    return shaderProgram;
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
    const char* vertexPath = "RenderScreen/vertex_shader.glsl";
    const char* fragmentPath = "RenderScreen/fragment_shader.glsl";
    
    glewInit();

    GLuint shaderProgram = createShaderProgram(vertexPath, fragmentPath);
        
    unsigned int VAO;
    unsigned int VBO;
    unsigned int EBO;
    
    // Vertex and Index data
    float vertices[] = {
        // Positions    // Texture Coords
        1.0f, -1.0f,     width, height,  // Bottom-left
        1.0f, 1.0f,    width, 0.0f,  // Bottom-right
        -1.0f, 1.0f,   0.0f, 0.0f,  // Top-right
        -1.0f, -1.0f,    0.0f, height   // Top-left
    };
    
    unsigned int indices[] = {
        0, 1, 2,  // First triangle
        2, 3, 0   // Second triangle
    };

    // Generate VAO, VBO, and EBO
    glGenVertexArrays(1, &VAO);
    glGenBuffers(1, &VBO);
    glGenBuffers(1, &EBO);

    // Bind VAO
    glBindVertexArray(VAO);

    // Bind and fill VBO with vertex data
    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

    // Bind and fill EBO with index data
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(indices), indices, GL_STATIC_DRAW);

    // Set up vertex position attribute
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);

    // Set up texture coordinate attribute
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void*)(2 * sizeof(float)));
    glEnableVertexAttribArray(1);

    // Unbind VAO (optional)
    //glBindVertexArray(0);
    
    unsigned int texID;
    glGenTextures (1, &texID);
    glBindTexture (GL_TEXTURE_RECTANGLE, texID);

    glTexImage2D (GL_TEXTURE_RECTANGLE_EXT, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
    /* Loop until the user closes the window */
    Py_BEGIN_ALLOW_THREADS
    while (!glfwWindowShouldClose(window))
    {
        //fprintf(stdout, data);
        /* Render here */
        glClear(GL_COLOR_BUFFER_BIT);
        //glRasterPos2f(-1,1);
        //glPixelZoom(1, -1);
        
        glUseProgram(shaderProgram);
        
        glBindVertexArray(VAO);
        
        // Update the texture with new data every frame
        glBindTexture(GL_TEXTURE_RECTANGLE, texID);
        glTexSubImage2D(GL_TEXTURE_RECTANGLE, 0, 0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, data);
        
        //glTexImage2D (GL_TEXTURE_RECTANGLE_EXT, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        //glDrawPixels(width, height, GL_RGBA, GL_UNSIGNED_BYTE, data);
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
