/**
 * Contract API Client
 *
 * Provides methods for interacting with contract-related endpoints
 * Follows Google Style Guide
 */

const ContractAPI = {
  /**
   * Template-related operations
   */
  template: {
    /**
     * Creates a new contract template
     * @param {Object} data - Template data
     * @param {string} data.templateCode - Unique template code
     * @param {string} data.templateName - Template name
     * @param {string} data.templateType - Template type
     * @param {string} data.description - Template description
     * @param {Object} data.schema - JSON schema for the template
     * @returns {Promise<Object>} Created template data
     */
    async create(data) {
      return await API.post('/templates', data);
    },

    /**
     * Gets a template by code
     * @param {string} templateCode - Template code
     * @returns {Promise<Object>} Template data
     */
    async get(templateCode) {
      return await API.get(`/templates/${templateCode}`);
    },

    /**
     * Lists all templates
     * @param {Object} params - Query parameters
     * @param {number} params.page - Page number
     * @param {number} params.size - Page size
     * @returns {Promise<Object>} List of templates
     */
    async list(params = {}) {
      return await API.get('/templates', params);
    },

    /**
     * Updates a template
     * @param {string} templateCode - Template code
     * @param {Object} data - Updated template data
     * @returns {Promise<Object>} Updated template data
     */
    async update(templateCode, data) {
      return await API.put(`/templates/${templateCode}`, data);
    },

    /**
     * Deletes a template
     * @param {string} templateCode - Template code
     * @returns {Promise<Object>} Deletion result
     */
    async delete(templateCode) {
      return await API.delete(`/templates/${templateCode}`);
    }
  },

  /**
   * Version-related operations
   */
  version: {
    /**
     * Creates a new version for a template
     * @param {string} templateId - Template ID
     * @param {Object} data - Version data
     * @param {string} data.versionNumber - Version number (e.g., "1.0")
     * @param {Object} data.schema - JSON schema for this version
     * @param {string} data.description - Version description
     * @returns {Promise<Object>} Created version data
     */
    async create(templateId, data) {
      return await API.post(`/templates/${templateId}/versions`, data);
    },

    /**
     * Gets a specific version
     * @param {string} templateId - Template ID
     * @param {string} versionId - Version ID
     * @returns {Promise<Object>} Version data
     */
    async get(templateId, versionId) {
      return await API.get(`/templates/${templateId}/versions/${versionId}`);
    },

    /**
     * Lists all versions for a template
     * @param {string} templateId - Template ID
     * @returns {Promise<Object>} List of versions
     */
    async list(templateId) {
      return await API.get(`/templates/${templateId}/versions`);
    },

    /**
     * Publishes a version
     * @param {string} templateId - Template ID
     * @param {string} versionId - Version ID
     * @returns {Promise<Object>} Publication result
     */
    async publish(templateId, versionId) {
      return await API.post(
        `/templates/${templateId}/versions/${versionId}/publish`
      );
    },

    /**
     * Deprecates a version
     * @param {string} templateId - Template ID
     * @param {string} versionId - Version ID
     * @returns {Promise<Object>} Deprecation result
     */
    async deprecate(templateId, versionId) {
      return await API.post(
        `/templates/${templateId}/versions/${versionId}/deprecate`
      );
    }
  },

  /**
   * Contract-related operations
   */
  contract: {
    /**
     * Creates a new contract from a template
     * @param {Object} data - Contract data
     * @param {string} data.templateCode - Template code
     * @param {string} data.templateVersion - Template version
     * @param {Object} data.formData - Contract form data
     * @returns {Promise<Object>} Created contract data
     */
    async create(data) {
      return await API.post('/contracts', data);
    },

    /**
     * Gets a contract by ID
     * @param {string} contractId - Contract ID
     * @returns {Promise<Object>} Contract data
     */
    async get(contractId) {
      return await API.get(`/contracts/${contractId}`);
    },

    /**
     * Lists contracts
     * @param {Object} params - Query parameters
     * @param {number} params.page - Page number
     * @param {number} params.size - Page size
     * @param {string} params.status - Filter by status
     * @returns {Promise<Object>} List of contracts
     */
    async list(params = {}) {
      return await API.get('/contracts', params);
    },

    /**
     * Updates a contract
     * @param {string} contractId - Contract ID
     * @param {Object} data - Updated contract data
     * @returns {Promise<Object>} Updated contract data
     */
    async update(contractId, data) {
      return await API.put(`/contracts/${contractId}`, data);
    },

    /**
     * Submits a contract for approval
     * @param {string} contractId - Contract ID
     * @returns {Promise<Object>} Submission result
     */
    async submit(contractId) {
      return await API.post(`/contracts/${contractId}/submit`);
    },

    /**
     * Approves a contract
     * @param {string} contractId - Contract ID
     * @returns {Promise<Object>} Approval result
     */
    async approve(contractId) {
      return await API.post(`/contracts/${contractId}/approve`);
    },

    /**
     * Rejects a contract
     * @param {string} contractId - Contract ID
     * @param {Object} data - Rejection data
     * @param {string} data.reason - Rejection reason
     * @returns {Promise<Object>} Rejection result
     */
    async reject(contractId, data) {
      return await API.post(`/contracts/${contractId}/reject`, data);
    },

    /**
     * Gets contract schema for rendering
     * @param {string} templateCode - Template code
     * @param {string} version - Template version
     * @returns {Promise<Object>} Schema data for rendering
     */
    async getSchema(templateCode, version) {
      return await API.get(
        `/templates/${templateCode}/versions/${version}/schema`
      );
    }
  },

  /**
   * Supplier-related operations
   */
  supplier: {
    /**
     * Searches suppliers
     * @param {Object} params - Search parameters
     * @param {string} params.keyword - Search keyword
     * @param {number} params.page - Page number
     * @param {number} params.size - Page size
     * @returns {Promise<Object>} List of suppliers
     */
    async search(params) {
      return await API.get('/suppliers/search', params);
    },

    /**
     * Gets supplier by ID
     * @param {string} supplierId - Supplier ID
     * @returns {Promise<Object>} Supplier data
     */
    async get(supplierId) {
      return await API.get(`/suppliers/${supplierId}`);
    }
  },

  /**
   * Attachment-related operations
   */
  attachment: {
    /**
     * Uploads an attachment
     * @param {File} file - File to upload
     * @param {string} contractId - Contract ID
     * @returns {Promise<Object>} Upload result
     */
    async upload(file, contractId) {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('contractId', contractId);

      const fullUrl = `${API.baseUrl}/attachments/upload`;

      try {
        const response = await fetch(fullUrl, {
          method: 'POST',
          body: formData
        });

        return await API.handleResponse(response);
      } catch (error) {
        return API.handleError(error);
      }
    },

    /**
     * Lists attachments for a contract
     * @param {string} contractId - Contract ID
     * @returns {Promise<Object>} List of attachments
     */
    async list(contractId) {
      return await API.get(`/contracts/${contractId}/attachments`);
    },

    /**
     * Deletes an attachment
     * @param {string} attachmentId - Attachment ID
     * @returns {Promise<Object>} Deletion result
     */
    async delete(attachmentId) {
      return await API.delete(`/attachments/${attachmentId}`);
    }
  }
};

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
  module.exports = ContractAPI;
}