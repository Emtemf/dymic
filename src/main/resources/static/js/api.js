/**
 * HTTP Request Wrapper
 *
 * Provides a clean API for making HTTP requests with error handling
 * Follows Google Style Guide
 */

const API = {
  /**
   * Candidate ports to try (in order of preference)
   */
  candidatePorts: [8888, 8080, 9090, 9999, 9000],

  /**
   * Current active port
   */
  currentPort: null,

  /**
   * Base URL for API requests (will be auto-detected)
   */
  baseUrl: null,

  /**
   * Default headers for all requests
   */
  defaultHeaders: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },

  /**
   * Makes an HTTP GET request
   * @param {string} url - The endpoint URL (relative to baseUrl)
   * @param {Object} params - Query parameters
   * @returns {Promise<Object>} Response data
   */
  async get(url, params = {}) {
    const fullUrl = new URL(`${this.baseUrl}${url}`);

    // Add query parameters
    Object.keys(params).forEach(key => {
      if (params[key] !== undefined && params[key] !== null) {
        fullUrl.searchParams.append(key, params[key]);
      }
    });

    try {
      const response = await fetch(fullUrl, {
        method: 'GET',
        headers: this.defaultHeaders
      });

      return await this.handleResponse(response);
    } catch (error) {
      return this.handleError(error);
    }
  },

  /**
   * Makes an HTTP POST request
   * @param {string} url - The endpoint URL (relative to baseUrl)
   * @param {Object} data - Request body data
   * @returns {Promise<Object>} Response data
   */
  async post(url, data = undefined) {
    const fullUrl = `${this.baseUrl}${url}`;

    try {
      const requestInit = {
        method: 'POST',
        headers: this.defaultHeaders
      };

      if (data !== undefined && data !== null) {
        requestInit.body = JSON.stringify(data);
      }

      const response = await fetch(fullUrl, requestInit);

      return await this.handleResponse(response);
    } catch (error) {
      return this.handleError(error);
    }
  },

  /**
   * Makes an HTTP PUT request
   * @param {string} url - The endpoint URL (relative to baseUrl)
   * @param {Object} data - Request body data
   * @returns {Promise<Object>} Response data
   */
  async put(url, data = {}) {
    const fullUrl = `${this.baseUrl}${url}`;

    try {
      const response = await fetch(fullUrl, {
        method: 'PUT',
        headers: this.defaultHeaders,
        body: JSON.stringify(data)
      });

      return await this.handleResponse(response);
    } catch (error) {
      return this.handleError(error);
    }
  },

  /**
   * Makes an HTTP DELETE request
   * @param {string} url - The endpoint URL (relative to baseUrl)
   * @returns {Promise<Object>} Response data
   */
  async delete(url) {
    const fullUrl = `${this.baseUrl}${url}`;

    try {
      const response = await fetch(fullUrl, {
        method: 'DELETE',
        headers: this.defaultHeaders
      });

      return await this.handleResponse(response);
    } catch (error) {
      return this.handleError(error);
    }
  },

  /**
   * Handles HTTP response
   * @param {Response} response - Fetch Response object
   * @returns {Promise<Object>} Parsed response data
   */
  async handleResponse(response) {
    // Try to parse JSON response
    let data = null;
    const contentType = response.headers.get('content-type');

    if (contentType && contentType.includes('application/json')) {
      try {
        data = await response.json();
      } catch (e) {
        // Response is not valid JSON
        data = null;
      }
    }

    // Check if response is successful
    if (response.ok) {
      return {
        success: true,
        data: data,
        status: response.status
      };
    }

    // Handle error responses
    return {
      success: false,
      error: data?.message || data?.error || 'Request failed',
      status: response.status,
      data: data
    };
  },

  /**
   * Handles network errors
   * @param {Error} error - Error object
   * @returns {Object} Error response object
   */
  handleError(error) {
    console.error('API Error:', error);

    return {
      success: false,
      error: error.message || 'Network error',
      status: 0
    };
  },

  /**
   * Sets authentication token
   * @param {string} token - JWT or other auth token
   */
  setAuthToken(token) {
    this.defaultHeaders['Authorization'] = `Bearer ${token}`;
  },

  /**
   * Removes authentication token
   */
  removeAuthToken() {
    delete this.defaultHeaders['Authorization'];
  },

  /**
   * Updates base URL
   * @param {string} url - New base URL
   */
  setBaseUrl(url) {
    this.baseUrl = url;
  },

  /**
   * Detects working backend port by trying each candidate port
   * @returns {Promise<number>} The working port number
   */
  async detectWorkingPort() {
    const sameOriginUrl = `${window.location.origin}/api/templates`;

    try {
      const response = await fetch(sameOriginUrl, {
        method: 'GET',
        headers: this.defaultHeaders
      });

      if (response.ok || response.status === 404) {
        const sameOrigin = new URL(window.location.origin);
        this.currentPort = sameOrigin.port ? Number(sameOrigin.port) : null;
        this.baseUrl = `${window.location.origin}/api`;
        console.log(`✓ Backend detected on current origin ${window.location.origin}`);
        return this.currentPort;
      }
    } catch (error) {
      console.log(`✗ Current origin ${window.location.origin} not available`);
    }

    for (const port of this.candidatePorts) {
      try {
        const testUrl = `http://localhost:${port}/api/templates`;
        const response = await fetch(testUrl, {
          method: 'GET',
          headers: this.defaultHeaders,
          timeout: 3000 // 3 second timeout
        });

        if (response.ok || response.status === 404) {
          // 404 is OK - means server is running, just no templates yet
          this.currentPort = port;
          this.baseUrl = `http://localhost:${port}/api`;
          console.log(`✓ Backend detected on port ${port}`);
          return port;
        }
      } catch (error) {
        // Connection failed, try next port
        console.log(`✗ Port ${port} not available`);
      }
    }

    // No working port found
    throw new Error('No backend server found on any candidate port');
  },

  /**
   * Initializes API with auto port detection
   * @returns {Promise<void>}
   */
  async initialize() {
    try {
      const port = await this.detectWorkingPort();
      console.log(`API initialized with port ${port}`);
      this.baseUrl = `http://localhost:${port}/api`;
      return port;
    } catch (error) {
      console.error('Failed to detect backend:', error);
      // Fallback to default port 8888
      this.currentPort = 8888;
      this.baseUrl = 'http://localhost:8888/api';
      return 8888;
    }
  },

  /**
   * Shows user notification about detected port
   * @param {number} port - Detected port number
   */
  showPortNotification(port) {
    // Create notification element
    const notification = document.createElement('div');
    notification.style.cssText = `
      position: fixed;
      top: 10px;
      right: 10px;
      background: #4CAF50;
      color: white;
      padding: 12px 20px;
      border-radius: 4px;
      box-shadow: 0 2px 5px rgba(0,0,0,0.3);
      z-index: 10000;
      font-family: Arial, sans-serif;
    `;
    notification.textContent = `✓ 已连接后端服务 (端口: ${port})`;
    document.body.appendChild(notification);

    // Auto remove after 3 seconds
    setTimeout(() => {
      notification.style.opacity = '0';
      notification.style.transition = 'opacity 0.5s';
      setTimeout(() => notification.remove(), 500);
    }, 3000);
  },

  /**
   * Shows error notification when backend not found
   */
  showErrorNotification() {
    const notification = document.createElement('div');
    notification.style.cssText = `
      position: fixed;
      top: 10px;
      right: 10px;
      background: #f44336;
      color: white;
      padding: 12px 20px;
      border-radius: 4px;
      box-shadow: 0 2px 5px rgba(0,0,0,0.3);
      z-index: 10000;
      font-family: Arial, sans-serif;
    `;
    notification.innerHTML = `
      ❌ 无法连接后端服务<br>
      请确保Spring Boot应用正在运行<br>
      <small>尝试端口: ${this.candidatePorts.join(', ')}</small>
    `;
    document.body.appendChild(notification);
  }
};

// Auto-initialize when script loads
if (typeof window !== 'undefined') {
  window.API = API;

  // Auto-detect port on page load
  window.addEventListener('DOMContentLoaded', async () => {
    try {
      const port = await API.initialize();
      if (typeof API.showPortNotification === 'function') {
        API.showPortNotification(port);
      }
    } catch (error) {
      console.error('Auto-initialization failed:', error);
      if (typeof API.showErrorNotification === 'function') {
        API.showErrorNotification();
      }
    }
  });
}